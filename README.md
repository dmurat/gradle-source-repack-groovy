## Running application
All commands should be executed from project root.

```
cd gradle-source-repack-groovy
./gradlew clean assemble
java -jar ./build/libs/gradle-source-repack-groovy-0.1.0-all.jar --root-log-level=INFO --log-level=DEBUG --cleanup=true 6.7.1
```

## GraalVM native image compilation
- Command executed from root project directory for creating GraalVM native image in `build/native-image` directory.
  - It is assumed that **Groovy 3.0.6** is used.
```
mkdir -p build/native-image && \
native-image --verbose \
--class-path build/libs/gradle-source-repack-groovy-0.1.0-all.jar:build/native-image-dependencies/classgraph-4.8.90.jar \
--allow-incomplete-classpath \
--report-unsupported-elements-at-runtime \
--initialize-at-build-time \
--initialize-at-run-time=org.codehaus.groovy.control.XStreamUtils,groovy.grape.GrapeIvy \
--no-fallback \
--no-server \
-H:ConfigurationFileDirectories=src/main/graal/ \
-H:+ReportExceptionStackTraces \
org.klokwrk.tools.gradle.source.repack.GradleSourceRepackCommand \
build/native-image/gradle-source-repack-groovy
```

- Command executed from root project directory for generating GraalVM native-image metadata in `build/native-image-configuration` directory.
  - Generated metadata are for information purposes only, when some more insight in GraalVM behavior is needed.
  - Only if needed, parts of generated metadata can be copied from `build/native-image-configuration/reflect-config.json` into `src/main/graal/reflect-config.json`.
  - In general, `src/main/graal/reflect-config.json` should remain empty.
```
java -agentlib:native-image-agent=config-output-dir=build/native-image-configuration/ -jar \
./build/libs/gradle-source-repack-groovy-0.1.0-all.jar -h
```

## Running native image
```
./build/native-image/gradle-source-repack-groovy --root-log-level=INFO --log-level=DEBUG --cleanup=true 6.7.1
```
