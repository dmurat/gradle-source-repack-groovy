package org.klokwrk.tools.gradle.source.repack.constants

import groovy.transform.CompileStatic

/**
 * Shared common constants.
 * <p/>
 * Any part of application can use these.
 */
@CompileStatic
class Constants {
  /**
   * Default Gradle distribution type.
   * <p/>
   * Used for calculating Gradle distribution archive name for download.
   */
  static final String GRADLE_DISTRIBUTION_TYPE_DEFAULT = "all"

  /**
   * Default extension of downloadable Gradle distribution archive name.
   */
  static final String GRADLE_DISTRIBUTION_FILE_EXTENSION_DEFAULT = ".zip"

  /**
   * Default URL prefix to use for downloading Gradle distribution archive.
   */
  static final String GRADLE_DISTRIBUTION_SITE_URL_DEFAULT = "https://services.gradle.org/distributions/"

  /**
   * Default directory into which Gradle distribution archive will be downloaded.
   * <p/>
   * Value corresponds to the current working directory from which command is started.
   */
  static final String DOWNLOAD_TARGET_DIR_DEFAULT = System.getProperty("user.dir")
}
