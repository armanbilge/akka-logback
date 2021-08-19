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
import ch.qos.logback.core.joran.action.Action
import ch.qos.logback.core.joran.spi.InterpretationContext
import ch.qos.logback.core.joran.util.PropertySetter
import ch.qos.logback.core.joran.util.beans.BeanDescriptionCache
import org.xml.sax.Attributes

/**
  * Logback [[ch.qos.logback.core.joran.action.Action]] to set the [[akka.actor.ActorSystem]] for an
  * [[ch.qos.logback.core.Appender]].
  */
class ActorSystemAction(system: ClassicActorSystemProvider, beanCache: BeanDescriptionCache)
    extends Action {

  override def begin(ic: InterpretationContext, name: String, attributes: Attributes): Unit = {
    val bean = ic.peekObject()
    val setter = new PropertySetter(beanCache, bean) {
      // Silences the warning in case `actorSystem` setter does not exist for this bean
      override def addWarn(msg: String): Unit = ()
    }
    setter.setContext(context)
    setter.setComplexProperty("actorSystem", system.classicSystem)
  }

  override def end(ic: InterpretationContext, name: String): Unit = ()

}
