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

import ch.qos.logback.core.joran.action.Action
import ch.qos.logback.core.joran.action.ActionUtil
import ch.qos.logback.core.joran.spi.InterpretationContext
import ch.qos.logback.core.util.OptionHelper
import com.typesafe.config.Config
import org.xml.sax.Attributes

/**
  * Logback [[ch.qos.logback.core.joran.action.Action]] to support `<akkaProperty>` tags.
  * Allows logback properties to be sourced from the Akka config.
  */
class AkkaPropertyAction(config: Config) extends Action {

  override def begin(
      context: InterpretationContext,
      elementName: String,
      attributes: Attributes
  ): Unit = {
    val name  = attributes.getValue(Action.NAME_ATTRIBUTE)
    val path  = attributes.getValue("path")
    val scope = ActionUtil.stringToScope(attributes.getValue(Action.SCOPE_ATTRIBUTE))
    if (OptionHelper.isEmpty(name) || OptionHelper.isEmpty(path))
      addError("""The "name" and "path" attributes of <akkaProperty> must be set""")
    ActionUtil.setProperty(context, name, config.getString(path), scope)
  }

  override def end(context: InterpretationContext, name: String): Unit = ()

}
