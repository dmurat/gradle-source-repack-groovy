package org.klokwrk.tools.gradle.source.repack

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.logging.LogLevel
import io.micronaut.logging.LoggingSystem
import org.klokwrk.tools.gradle.source.repack.checksum.GradleSha256CheckInfo
import org.klokwrk.tools.gradle.source.repack.checksum.GradleSha256Checker
import org.klokwrk.tools.gradle.source.repack.downloader.GradleDownloader
import org.klokwrk.tools.gradle.source.repack.downloader.GradleDownloaderInfo
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

  @Inject
  GradleDownloader gradleDownloader

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

    File gradleDistributionZipFile = fetchGradleDistributionZipFile(cliArguments, gradleDownloader)
    File gradleDistributionZipSha256File = fetchGradleDistributionZipSha256File(cliArguments, gradleDownloader)

    GradleSha256CheckInfo gradleSha256CheckInfo = GradleSha256Checker.checkSha256(gradleDistributionZipSha256File, gradleDistributionZipFile)
    if (gradleSha256CheckInfo.isMatch()) {
      println "SHA-256 checksum OK."
    }
    else {
      String message = "SHA-256 does not match [fetched: ${ gradleSha256CheckInfo.fetchedSha256 }, calculated: ${ gradleSha256CheckInfo.calculatedSha256 }]. Cannot continue."
      throw new IllegalStateException(message)
    }

    if (cliArguments.performCleanup) {
      cleanDownloadedFiles([gradleDistributionZipFile, gradleDistributionZipSha256File])
    }

    log.debug "Finished."
  }

  private static File fetchGradleDistributionZipFile(GradleSourceRepackCliArguments cliArguments, GradleDownloader gradleDownloader) {
    GradleDownloaderInfo gradleDownloaderZipInfo = cliArguments.toGradleDownloaderInfoForDistributionZip()

    File gradleDistributionZipFile = new File(gradleDownloaderZipInfo.downloadTargetFileAbsolutePath)
    if (gradleDistributionZipFile.exists()) {
      log.debug "Using already existing Gradle distribution ZIP file '${ gradleDistributionZipFile.absolutePath }'."
    }
    else {
      log.debug "Starting download of Gradle distribution ZIP file."
      gradleDistributionZipFile = gradleDownloader.download(gradleDownloaderZipInfo)
    }

    return gradleDistributionZipFile
  }

  private static File fetchGradleDistributionZipSha256File(GradleSourceRepackCliArguments cliArguments, GradleDownloader gradleDownloader) {
    GradleDownloaderInfo gradleDownloaderZipSha256Info = cliArguments.toGradleDownloaderInfoForDistributionZipSha256File()

    File gradleDistributionZipSha256File = new File(gradleDownloaderZipSha256Info.downloadTargetFileAbsolutePath)
    if (gradleDistributionZipSha256File.exists()) {
      log.debug "Using already existing Gradle distribution's SHA-256 file '${ gradleDistributionZipSha256File.absolutePath }'."
    }
    else {
      log.debug "Starting download of Gradle distribution's SHA-256 file."
      gradleDistributionZipSha256File = gradleDownloader.download(gradleDownloaderZipSha256Info)
    }

    return gradleDistributionZipSha256File
  }

  private static void cleanDownloadedFiles(List<File> fileListToDelete) {
    log.debug "Deleting downloaded files: ${ fileListToDelete }"
    fileListToDelete.each (File file) -> file.delete()
  }
}
