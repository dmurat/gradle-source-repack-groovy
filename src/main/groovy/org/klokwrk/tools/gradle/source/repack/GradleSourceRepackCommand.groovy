package org.klokwrk.tools.gradle.source.repack

import groovy.transform.CompileStatic
import io.micronaut.configuration.picocli.PicocliRunner
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(name = 'gradle-source-repack-groovy', description = '...', mixinStandardHelpOptions = true)
@CompileStatic
class GradleSourceRepackCommand implements Runnable {

  @Option(names = ['-v', '--verbose'], description = '...')
  boolean verbose

  static void main(String[] args) throws Exception {
    PicocliRunner.run(GradleSourceRepackCommand.class, args)
  }

  void run() {
    // business logic here
    if (verbose) {
      println "Hi!"
    }
  }
}
