package org.klokwrk.tools.gradle.source.repack.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/**
 * Programmatically registers classes acquired via reflection.
 * <p/>
 * Used only during compilation time of GraalVM native image. It is auto-discovered by native image compiler. Needs to be written in Java.
 */
@SuppressWarnings("unused")
@AutomaticFeature
public class RuntimeReflectionRegistrationFeature implements Feature {
  @Override
  public void beforeAnalysis(BeforeAnalysisAccess access) {
    int[] dgmClassNumbersToRegister = { 1125 };

    try {
      for (int dgmClassNumberToRegister : dgmClassNumbersToRegister) {
        Class<?> someClass = Class.forName("org.codehaus.groovy.runtime.dgm$" + dgmClassNumberToRegister);
        RuntimeReflection.register(someClass);
        RuntimeReflection.register(someClass.getConstructors());
        RuntimeReflection.register(someClass.getMethods());
      }
    }
    catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
