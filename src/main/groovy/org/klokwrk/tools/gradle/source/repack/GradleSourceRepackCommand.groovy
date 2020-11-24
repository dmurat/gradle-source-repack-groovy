package org.klokwrk.tools.gradle.source.repack

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.logging.LogLevel
import io.micronaut.logging.LoggingSystem
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Visibility
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.ParameterException
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec

import javax.inject.Inject
import java.util.regex.Pattern

@Command(
    name = "gradle-source-repack-groovy",
    description = "Downloads Gradle source distribution and repackages it in a JAR suitable to use for debugging Gradle internals in IDEA.",
    mixinStandardHelpOptions = true
)
@Slf4j
@CompileStatic
class GradleSourceRepackCommand implements Runnable {
  private static final String GRADLE_VERSION_REGEX_FORMAT = /([2-9]\d*){1}(\.\d+){1}(\.\d+)?(-([a-zA-Z1-9]+))?/
  private static final Pattern GRADLE_VERSION_REGEX_PATTERN = ~GRADLE_VERSION_REGEX_FORMAT

  private String cliParameterGradleVersion

  @Spec
  private CommandSpec commandSpec

  @SuppressWarnings("unused")
  @Parameters(paramLabel = "<gradle-version>", description = "Gradle version to use.")
  void setCliParameterGradleVersion(String gradleVersion) {
    if (!(gradleVersion ==~ GRADLE_VERSION_REGEX_PATTERN)) {
      throw new ParameterException(
          commandSpec.commandLine(),
          "Invalid value '${ gradleVersion }' for parameter '<gradle-version>'. Value should comply with regex '${ GRADLE_VERSION_REGEX_FORMAT }'."
      )
    }

    this.cliParameterGradleVersion = gradleVersion
  }

  @Option(
      names = ["-L", "--root-log-level"], paramLabel = "<ERROR|WARN|INFO|...>", showDefaultValue = Visibility.ALWAYS,
      description = "Setup root log level. Changing a default value is effective only on logging happening after a command starts running. All preceding logging is not affected."
  )
  LogLevel cliOptionRootLogLevel = LogLevel.WARN

  @Option(names = ["-l", "--log-level"], description = "Setup gradle-source-repack specific log level.", paramLabel = "<ERROR|WARN|INFO|...>", showDefaultValue = Visibility.ALWAYS)
  LogLevel cliOptionGradleSourceRepackLogLevel = LogLevel.WARN

  @Option(names = ["-c", "--cleanup"], description = "Removing downloaded files after successful execution.", showDefaultValue = Visibility.ALWAYS, arity = "1", paramLabel = "<true|false>")
  Boolean cliOptionCleanup = true

  @Inject
  LoggingSystem loggingSystem

  static void main(String[] args) throws Exception {
    PicocliRunner.run(GradleSourceRepackCommand, args)
  }

  void run() {
    if (cliOptionRootLogLevel != LogLevel.WARN) {
      loggingSystem.setLogLevel("ROOT", cliOptionRootLogLevel)
    }

    if (cliOptionGradleSourceRepackLogLevel != LogLevel.WARN) {
      loggingSystem.setLogLevel("org.klokwrk.tools.gradle.source.repack", cliOptionGradleSourceRepackLogLevel)
    }

    log.debug "Started."

    GradleSourceRepackCliArguments cliArguments = new GradleSourceRepackCliArguments(cliParameterGradleVersion)
    cliArguments.performCleanup = cliOptionCleanup
    log.debug "cliArguments: $cliArguments"

    log.debug "Finished."
  }
}
