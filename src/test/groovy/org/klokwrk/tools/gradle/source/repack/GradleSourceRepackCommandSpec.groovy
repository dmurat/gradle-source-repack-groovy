package org.klokwrk.tools.gradle.source.repack

import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment

import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class GradleSourceRepackCommandSpec extends Specification {

  @Shared
  @AutoCleanup
  ApplicationContext ctx = ApplicationContext.run(Environment.CLI, Environment.TEST)

  void "test gradle-source-repack-groovy with command line option"() {
    given:
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
    System.setOut(new PrintStream(byteArrayOutputStream))

    String[] args = ['-v'] as String[]
    PicocliRunner.run(GradleSourceRepackCommand, ctx, args)

    expect:
    byteArrayOutputStream.toString().contains('Hi!')
  }
}
