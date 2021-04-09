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

import akka.actor.{ Actor, Status }
import akka.event.Logging
import akka.event.Logging.{ InitializeLogger, LoggerInitialized }
import com.armanbilge.akkalogback.AkkaConfigurator.checkResource

import scala.util.Try

/**
  * A fake (no-op) logger whose initialization is used to register the [[akka.actor.ActorSystem]] with [[AkkaConfigurator]].
  */
class AkkaLogback extends Actor {

  private val log = Logging(this)

  override def receive: Receive = {
    case InitializeLogger(_) =>
      if (sys.props.contains("logback.configurationFile"))
        log.warning(
          "akka-logback disabled because system property [logback.configurationFile] is set"
        )
      List("logback-test.xml", "logback.groovy", "logback.xml").foreach { name =>
        checkResource(name).foreach { url =>
          log.warning(s"akka-logback disabled because found resource [$name] at [$url]")
        }
      }

      val initialized = Try {
        AkkaConfigurator.registerActorSystem(context.system)
        LoggerInitialized
      } recover { case ex =>
        Status.Failure(ex)
      }
      sender() ! initialized.get
    case _ => // Do nothing
  }
}
