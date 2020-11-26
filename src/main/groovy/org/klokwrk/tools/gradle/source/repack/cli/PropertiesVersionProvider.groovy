package org.klokwrk.tools.gradle.source.repack.cli

import picocli.CommandLine.IVersionProvider
import groovy.transform.CompileStatic

@CompileStatic
class PropertiesVersionProvider implements IVersionProvider {
  @Override
  String[] getVersion() throws Exception {
    URL url = getClass().getResource("/version.properties")
    if (!url) {
      return ["Version info is not available."] as String[]
    }

    Properties properties = new Properties()
    properties.load(url.newInputStream())

    return ["${ properties["moduleName"] } ${ properties["moduleVersion"] }"] as String[]
  }
}
