# akka-logback

![Build Status](https://github.com/armanbilge/akka-logback/workflows/Build/badge.svg)

akka-logback helps integrate [logback](https://logback.qos.ch/) with your [Akka](https://akka.io) applications.  Features:
* Allows logback properties to be sourced from the Akka configuration
* Provides logback appenders access to the actor system

## Installation

The artifact is published to Maven Central.

```scala
libraryDependencies += "com.armanbilge" %% "akka-logback" % "0.1.0"
```

## Usage

Enable akka-logback in your `application.conf`.

```hocon
# AkkaLogback must be initialized before Slf4jLogger
akka.loggers = ["com.armanbilge.akkalogback.AkkaLogback", "akka.event.slf4j.Slf4jLogger"]
```

Your logback configuration is then automatically loaded from one of the following locations, ranked by priority.
1. The path specified by the `akka-logback.configuration-file` setting in your `application.conf`
2. `logback-akka-test.xml` in the class path
3. `logback-akka.xml` in the class path

Note that if `logback.xml`, `logback-test.xml`, or `logback.groovy` are on the class path or the system property `logback.configurationFile` is set, then akka-logback will be bypassed and logback will initialize normally (i.e., without the Akka extensions).
Configuration via Groovy is not supported at this time.

Now you can source [logback properties](https://logback.qos.ch/manual/configuration.html#variableSubstitution) from your Akka configuration.

```xml
<configuration>
    <!-- Sets the variable `AKKA_LOGLEVEL` to the value of the `akka.loglevel` setting defined in the Akka configuration -->
    <akkaProperty name="AKKA_LOGLEVEL" path="akka.loglevel" />
    
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Sets the root logger level to the value of the `AKKA_LOGLEVEL` variable via substitution -->
    <root level="${AKKA_LOGLEVEL}">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

See the [Akka documentation](https://doc.akka.io/docs/akka/current/typed/logging.html#logback) for guidance on configuring logback.

### Implementing Akka-based appenders

To implement a logback appender that has access to the `ActorSystem`, add a setter method to your appender with the following signature.
akka-logback will use it to provide the `ActorSystem` to your appender during initialization before its `start()` method is invoked.

```scala
def setActorSystem(actorSystem: akka.actor.ActorSystem): Unit
```

For an example of this, see the [Google Cloud Logging Appender](https://github.com/armanbilge/alpakka/blob/google-cloud-logging/google-cloud-logging/src/main/scala/akka/stream/alpakka/googlecloud/logging/logback/CloudLoggingAppender.scala).

## Contribution policy

Contributions via GitHub pull requests are gladly accepted from their original author.
Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license.
Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License

This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
