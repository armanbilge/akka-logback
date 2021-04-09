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

import akka.actor.ActorSystem
import akka.testkit.TestKit
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.UnsynchronizedAppenderBase
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{ Logger, LoggerFactory }

import scala.beans.BeanProperty

class MyAkkaAppender extends UnsynchronizedAppenderBase[ILoggingEvent] {

  @BeanProperty var actorSystem: ActorSystem = _

  override def append(eventObject: ILoggingEvent): Unit = ()
}

class AkkaLogbackSpec
    extends TestKit(ActorSystem("AkkaLogbackSpec"))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  val root    = context.getLogger(Logger.ROOT_LOGGER_NAME)

  "AkkaLogback" should {
    "have set akka.loglevel for the root logger" in {
      root.getLevel.levelStr shouldEqual system.settings.LogLevel
    }
    "have put the Akka property in the context" in {
      context.getProperty("MY_AKKA_PROPERTY") shouldEqual system.settings.config.getString(
        "my-app.my-setting"
      )
    }
    "have set the ActorSystem on the appender" in {
      val myAppender = root.getAppender("MY_AKKA_APPENDER").asInstanceOf[MyAkkaAppender]
      myAppender.actorSystem shouldEqual system
    }
  }
}
