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
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.{ ElementSelector, RuleStore }

/**
  * Extends logback's [[ch.qos.logback.classic.joran.JoranConfigurator]] with additional
  * Akka-specific parsing rules:
  *   1. [[ActorSystemAction]] for setting [[akka.actor.ActorSystem]] on an
  *      [[ch.qos.logback.core.Appender]]
  *   1. [[AkkaPropertyAction]] for sourcing a logback property from the Akka configuration
  */
class AkkaJoranConfigurator(system: ClassicActorSystemProvider) extends JoranConfigurator {

  override def addInstanceRules(rs: RuleStore): Unit = {
    super.addInstanceRules(rs)
    rs.addRule(
      new ElementSelector("configuration/appender"),
      new ActorSystemAction(system, getBeanDescriptionCache)
    )
    rs.addRule(
      new ElementSelector("configuration/akkaProperty"),
      new AkkaPropertyAction(system.classicSystem.settings.config)
    )
  }

}
