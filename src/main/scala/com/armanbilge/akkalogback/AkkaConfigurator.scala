/*
 * Copyright 2021 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.armanbilge.akkalogback

import akka.actor.ClassicActorSystemProvider
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.core.LogbackException
import ch.qos.logback.core.spi.ContextAwareBase
import ch.qos.logback.core.status.{ ErrorStatus, InfoStatus, WarnStatus }
import com.armanbilge.akkalogback.AkkaConfigurator.{
  AutoConfigFile,
  ConfigFilePath,
  ExistingFile,
  Resource,
  TestAutoConfigFile,
  WellFormedUrl,
  checkResource,
  checkResources,
  configurationTimeout,
  registrationLatch
}
import com.typesafe.config.ConfigFactory

import java.io.File
import java.net.URL
import java.util.concurrent.{ CountDownLatch, TimeUnit }
import scala.annotation.nowarn
import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

object AkkaConfigurator {
  private val configurationTimeout =
    ConfigFactory.load().getDuration("akka-logback.configuration-timeout")
  private val registrationLatch                            = new CountDownLatch(1)
  @volatile private var system: ClassicActorSystemProvider = _

  def registerActorSystem(system: ClassicActorSystemProvider): Unit =
    if (this.system == null) {
      this.system = system
      registrationLatch.countDown()
    } else
      throw new IllegalStateException("ActorSystem has been already registered")

  private val AutoConfigFile     = "logback-akka.xml"
  private val TestAutoConfigFile = "logback-akka-test.xml"
  private val ConfigFilePath     = "akka-logback.configuration-file"

  private[akkalogback] def checkResource(
      name: String
  )(implicit context: LoggerContext = null, loader: ClassLoader = getClass.getClassLoader) =
    checkResources(name) { case Resource(url) =>
      url
    }

  private[akkalogback] def checkResources(name: String)(
      toUrl: PartialFunction[String, URL]
  )(implicit
      context: LoggerContext = null,
      loader: ClassLoader = getClass.getClassLoader
  ): Option[URL] = {
    val url = toUrl.lift(name)
    Option(context).map(_.getStatusManager).foreach { sm =>
      url match {
        case None =>
          sm.add(new InfoStatus(s"Could NOT find resource [$name]", context))
        case Some(url) =>
          sm.add(new InfoStatus(s"Found resource [$name] at [$url]", context))
          Try(loader.getResources(name).asScala.toSet: @nowarn("msg=deprecated")) match {
            case Success(urls) if urls.size > 1 =>
              sm.add(
                new WarnStatus(s"Resource [$name] occurs multiple times on the classpath.", context)
              )
              urls.foreach { url =>
                sm.add(new WarnStatus(s"Resource [$name] occurs at [$url]", context))
              }
            case Failure(ex) =>
              sm.add(new ErrorStatus(s"Failed to get url list for resource [$name]", context, ex))
            case _ =>
          }
      }
    }
    url
  }

  private object WellFormedUrl {
    def unapply(url: String): Option[URL] =
      Try(new URL(url)).toOption
  }

  private object Resource {
    def unapply(name: String)(implicit loader: ClassLoader): Option[URL] =
      Try(Option(loader.getResource(name))).toOption.flatten
  }

  private object ExistingFile {
    def unapply(path: String): Option[URL] =
      Some(new File(path))
        .filter(f => f.exists && f.isFile)
        .flatMap(f => Try(f.toURI.toURL).toOption)
  }
}

class AkkaConfigurator extends ContextAwareBase with Configurator {

  override def configure(loggerContext: LoggerContext): Unit = {
    registrationLatch.await(configurationTimeout.toMillis, TimeUnit.MILLISECONDS)

    implicit val context = loggerContext
    implicit val loader  = getClass.getClassLoader

    if (AkkaConfigurator.system == null)
      throw new LogbackException("No ActorSystem was registered")
    val system = AkkaConfigurator.system.classicSystem
    val config = system.settings.config

    val logbackConfigFile = Some(config.getString(ConfigFilePath)).filterNot(_.isEmpty)
    logbackConfigFile
      .flatMap(checkResources(_) {
        case WellFormedUrl(url) => url
        case Resource(url)      => url
        case ExistingFile(url)  => url
      })
      .orElse(checkResource(TestAutoConfigFile))
      .orElse(checkResource(AutoConfigFile)) match {
      case Some(url) if url.toString.endsWith("xml") =>
        val configurator = new AkkaJoranConfigurator(system)
        configurator.setContext(loggerContext)
        configurator.doConfigure(url)
      case Some(url) =>
        throw new LogbackException(s"Unexpected filename extension of file [$url]. Should be .xml")
      case None =>
        throw new LogbackException(
          s"Could not find ${logbackConfigFile.fold("")(f => s"[$f], ")}[logback-akka-test.xml] or [logback-akka.xml]"
        )
    }
  }
}
