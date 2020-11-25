package org.klokwrk.tools.gradle.source.repack.graal;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.graalvm.nativeimage.hosted.Feature;

/**
 * Programmatically registers default Groovy methods (accessed by reflection) with Graal native image compiler.
 * <p/>
 * Default groovy method classes are {@code org.codehaus.groovy.runtime.dgm$number.class}.
 * <p/>
 * This class is used during compilation of GraalVM native image. It is auto-discovered by native image compiler. Needs to be written in Java.
 */
@SuppressWarnings("unused")
@AutomaticFeature
public class GroovyDefaultMethodsClassesRegistrationFeature implements Feature {
  @Override
  public void beforeAnalysis(BeforeAnalysisAccess beforeAnalysisAccess) {
    String groovyRuntimePackage = "org.codehaus.groovy.runtime";

    ClassGraph groovyRuntimeClassGraph = new ClassGraph()
        .enableClassInfo()
        .acceptPackages(groovyRuntimePackage);

    try (ScanResult scanResult = groovyRuntimeClassGraph.scan()) {
      ClassInfoList defaultGroovyMethodClassInfoCandidateList = scanResult.getSubclasses("org.codehaus.groovy.reflection.GeneratedMetaMethod");
      ClassInfoList filteredGroovyMethodClassInfoCandidateList =
          defaultGroovyMethodClassInfoCandidateList
              .filter((ClassInfo defaultGroovyMethodClassInfoCandidate) -> defaultGroovyMethodClassInfoCandidate.getName().matches("^org.codehaus.groovy.runtime.dgm\\$[0-9]+$"));

      RegistrationFeatureUtils.registerClasses(filteredGroovyMethodClassInfoCandidateList);
    }
  }
}
