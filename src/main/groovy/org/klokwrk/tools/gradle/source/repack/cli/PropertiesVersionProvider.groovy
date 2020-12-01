package org.klokwrk.tools.gradle.source.repack.cli

import picocli.CommandLine.IVersionProvider
import groovy.transform.CompileStatic

/**
 * Picocli version provider.
 * <p/>
 * Reads a version from {@code version.properties} file in classpath.
 */
@CompileStatic
class PropertiesVersionProvider implements IVersionProvider {
  @Override
  String[] getVersion() throws Exception {
    URL url = getClass().getResource("/version.properties")
    if (!url) {
      return ["Version info is not available."] as String[]
    }

    Properties properties = new Properties()
    url.newInputStream().withCloseable { BufferedInputStream bufferedInputStream ->
      properties.load(bufferedInputStream)
    }

    return ["${ properties["moduleName"] } ${ properties["moduleVersion"] }"] as String[]
  }
}
