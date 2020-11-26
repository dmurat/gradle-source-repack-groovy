package org.klokwrk.tools.gradle.source.repack.checksum

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Checks SHA-256 checksum of Gradle distribution ZIP file.
 */
@Slf4j
@CompileStatic
class GradleSha256Checker {
  /**
   * Calculates SHA-256 checksum of Gradle distribution ZIP file and compares it to the provided file containing SHA-256 hex encoded checksum.
   */
  static GradleSha256CheckInfo checkSha256(File gradleDistributionZipSha256File, File gradleDistributionZipFile) {
    String fetchedSha256 = gradleDistributionZipSha256File.text
    log.debug "Fetched SHA-256   : ${ fetchedSha256 }"

    String calculatedSha256 = ChecksumCalculator.calculateAsHexEncodedString(gradleDistributionZipFile, "SHA-256")
    log.debug "Calculated SHA-256: ${ calculatedSha256 }"

    return new GradleSha256CheckInfo(fetchedSha256, calculatedSha256)
  }
}
