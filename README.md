## Feature http-client documentation
- [Micronaut Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

## GraalVM native image compilation
- Command executed from root project directory for generating GraalVM native-image metadata in `build/native-image-configuration` directory.
  Only use the required minimum (application specific classes) from `reflect-config.json` and copy it into `src/main/graal/reflect-config.json`.
```
java -agentlib:native-image-agent=config-output-dir=build/native-image-configuration/ -jar \
./build/libs/gradle-source-repack-groovy-0.1.0-all.jar -h
```

- Command executed from root project directory for creating GraalVM native image in `build/native-image` directory.
```
mkdir -p build/native-image && \
native-image --class-path build/libs/gradle-source-repack-groovy-0.1.0-all.jar \
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
