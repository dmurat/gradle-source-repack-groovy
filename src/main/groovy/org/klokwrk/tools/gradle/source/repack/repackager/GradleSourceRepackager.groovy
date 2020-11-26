package org.klokwrk.tools.gradle.source.repack.repackager

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Repackages Gradle sources into a sources archive understood by IDEA.
 */
@Slf4j
@CompileStatic
class GradleSourceRepackager {
  static void repackGradleSource(GradleSourceRepackagerInfo repackagerInfo) {
    log.debug("Starting repackaging with following gradleSourceRepackagerInfo: ${ repackagerInfo }")
    log.debug("Repackaging source and target: '{}' ==> '{}'.", repackagerInfo.gradleDistributionZipFilePath, repackagerInfo.gradleApiSourcesFilePath)

    File gradleApiDir = new File(repackagerInfo.gradleApiDirPath)
    if (!gradleApiDir.exists()) {
      throw new IllegalStateException("Target directory for repacking does not exist: [${ gradleApiDir.absolutePath }]")
    }

    log.info "Repackaging Gradle sources: ${ repackagerInfo.gradleDistributionZipFilePath } ===> ${ repackagerInfo.gradleApiSourcesFilePath }"

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(repackagerInfo.getGradleApiSourcesFilePath())))) {
      try (ZipFile zipFile = new ZipFile(repackagerInfo.getGradleDistributionZipFilePath())) {
        String filterPrefix = "gradle-${ repackagerInfo.gradleVersion }/src/"

        Long countOfTargetZipEntries = zipFile
            .stream()
            .filter((ZipEntry zipEntry) -> !zipEntry.isDirectory() && zipEntry.name.startsWith(filterPrefix))
            .map((ZipEntry zipEntry) -> calculateTargetZipEntryName(filterPrefix, zipEntry))
            .distinct() // skipping duplicate target entries (e.g. package-info.java)
            .count()

        AtomicLong zipEntriesProcessedCount = new AtomicLong(0)
        zipFile.stream()
               .filter((ZipEntry zipEntry) -> !zipEntry.isDirectory() && zipEntry.name.startsWith(filterPrefix))
               .forEach((ZipEntry originalZipEntry) -> {
                 String targetZipEntryName = calculateTargetZipEntryName(filterPrefix, originalZipEntry)

                 String skippedMessage = null
                 try (InputStream inputStream = zipFile.getInputStream(originalZipEntry)) {
                   try {
                     zipOutputStream.putNextEntry(new ZipEntry(targetZipEntryName))

                     byte[] bytes = new byte[1024]
                     int length
                     while ((length = inputStream.read(bytes)) >= 0) {
                       zipOutputStream.write(bytes, 0, length)
                     }
                   }
                   catch (ZipException zipException) {
                     if (zipException.getMessage().contains("duplicate entry")) {
                       skippedMessage = "Skipping ${ zipException.getMessage() }."
                     }
                     else {
                       throw zipException
                     }
                   }
                 }

                 if (!skippedMessage) {
                   zipEntriesProcessedCount.accumulateAndGet(1, Long::sum)
                 }

                 Integer percentage = zipEntriesProcessedCount.get() * 100 / countOfTargetZipEntries as Integer
                 Boolean isLastEntry = countOfTargetZipEntries == zipEntriesProcessedCount.get()
                 String newLineIfNecessary = (isLastEntry || (skippedMessage && log.debugEnabled) || log.traceEnabled) ? "\n" : ""
                 print "\rRepackaging into ${ repackagerInfo.gradleApiSourcesFilePath }: ${ percentage }%${ newLineIfNecessary }"

                 if (skippedMessage) {
                   log.debug skippedMessage
                 }
                 else {
                   log.trace "Repacked Gradle source file: {} -> {}", originalZipEntry.getName(), targetZipEntryName
                 }
               })
      }
    }
  }

  private static String calculateTargetZipEntryName(String filterPrefix, ZipEntry originalZipEntry) {
    String sourceZipEntryFullName = originalZipEntry.name
    String sourceZipEntryWithoutPrefix = sourceZipEntryFullName - filterPrefix

    String targetZipEntryName = sourceZipEntryWithoutPrefix.substring(sourceZipEntryWithoutPrefix.indexOf("/") + 1)
    return targetZipEntryName
  }
}
