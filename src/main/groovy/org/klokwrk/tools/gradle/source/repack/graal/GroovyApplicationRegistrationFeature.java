package org.klokwrk.tools.gradle.source.repack.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;

/**
 * Programmatically registers application's reflective Groovy classes with Graal native image compiler.
 * <p/>
 * This class is used during compilation of GraalVM native image. It is auto-discovered by native image compiler. Needs to be written in Java.
 */
@SuppressWarnings("unused")
@AutomaticFeature
public class GroovyApplicationRegistrationFeature implements Feature {
  @Override
  public void beforeAnalysis(BeforeAnalysisAccess beforeAnalysisAccess) {
    String[] gradleSourceRepackPackage = new String[] { "org.klokwrk.tools.gradle.source.repack" };

    ClassGraph gradleSourceRepackClassGraph = new ClassGraph()
        .enableClassInfo()
        .enableAnnotationInfo()
        .acceptPackages(gradleSourceRepackPackage);

    try (ScanResult scanResult = gradleSourceRepackClassGraph.scan()) {
      registerGeneratedClosureClasses(scanResult);
      registerImmutableClasses(scanResult);
      registerPicocliClasses(scanResult);
    }
  }

  /**
   * Registers generated Groovy closure classes with Graal native image compiler.
   * <p/>
   * For some well known Groovy methods that take closures as parameters (i.e. each), Groovy generates helper classes in the fly next to the class that uses these methods with closure parameters.
   * For closures calls to work correctly, Groovy generated helper classes needs to be registered with GraalVM native image compiler.
   */
  public static void registerGeneratedClosureClasses(ScanResult scanResult) {
    ClassInfoList generatedGroovyClosureClassInfoList = scanResult.getClassesImplementing("org.codehaus.groovy.runtime.GeneratedClosure");
    RegistrationFeatureUtils.registerClasses(generatedGroovyClosureClassInfoList);
  }

  /**
   * Registers Groovy immutable classes with Graal native image compiler.
   */
  public static void registerImmutableClasses(ScanResult scanResult) {
    ClassInfoList groovyImmutableClassInfoList = scanResult.getClassesWithAnnotation("groovy.transform.KnownImmutable");
    RegistrationFeatureUtils.registerClasses(groovyImmutableClassInfoList);
  }

  /**
   * Registers Groovy picocli classes with Graal native image compiler.
   */
  public static void registerPicocliClasses(ScanResult scanResult) {
    ClassInfoList groovyPicocliClassInfoList = scanResult.getClassesWithAnnotation("picocli.CommandLine$Command");
    RegistrationFeatureUtils.registerClasses(groovyPicocliClassInfoList);
  }
}
