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

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import ch.qos.logback.classic.LoggerContext
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.{ Logger, LoggerFactory }

// The addition of this spec is motivated by race conditions in Akka typed
// where SLF4J (and thereby logback) may initialize before AkkaLogback registers
// the ActorSystem with the AkkaConfigurator.
// TODO Is there a way to reliably test for this race condition?
class AkkaTypedLogbackSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  val system  = ActorSystem(Behaviors.ignore, "AkkaTypedLogbackSpec")
  val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  val root    = context.getLogger(Logger.ROOT_LOGGER_NAME)

  override def afterAll(): Unit = system.terminate()

  "AkkaLogback" should {
    "have set the ActorSystem on the appender" in {
      val myAppender = root.getAppender("MY_AKKA_APPENDER").asInstanceOf[MyAkkaAppender]
      myAppender.actorSystem shouldEqual system.classicSystem
    }
  }
}
