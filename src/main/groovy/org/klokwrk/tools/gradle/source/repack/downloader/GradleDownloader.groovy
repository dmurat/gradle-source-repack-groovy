package org.klokwrk.tools.gradle.source.repack.downloader

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.core.io.buffer.ByteBuffer
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.reactivex.internal.functions.Functions

import javax.inject.Singleton

@Singleton
@Slf4j
@CompileStatic
class GradleDownloader {
  RxStreamingHttpClient streamingHttpClient
  HttpClient headOnlyHttpClient

  GradleDownloader(@Client RxStreamingHttpClient streamingHttpClient, @Client("head-only") HttpClient headOnlyHttpClient) {
    this.streamingHttpClient = streamingHttpClient
    this.headOnlyHttpClient = headOnlyHttpClient
  }

  File download(GradleDownloaderInfo gradleDownloaderInfo) {
    log.debug "Starting download with following gradleDownloaderInfo: $gradleDownloaderInfo"

    HttpResponse<?> headResponse = streamingHttpClient.toBlocking().exchange(HttpRequest.HEAD(gradleDownloaderInfo.fullDownloadUrl))
    HttpStatus headResponseStatus = headResponse.getStatus()

    List<HttpStatus> expectedHttpStatusList = [HttpStatus.OK, HttpStatus.MOVED_PERMANENTLY]
    if (headResponseStatus !in expectedHttpStatusList) {
      String message = "HEAD request for '${ gradleDownloaderInfo.fullDownloadUrl }' didn't returned any of expected HTTP response statuses " +
                       "[${ expectedHttpStatusList.collect({ "${ it.reason }(${ it.code })" }).join(", ") }]. Cannot continue."
      throw new IllegalStateException(message)
    }

    String realDownloadUrl = gradleDownloaderInfo.fullDownloadUrl
    if (HttpStatus.MOVED_PERMANENTLY == headResponseStatus) {
      String newLocationUrl = headResponse.header("Location")
      realDownloadUrl = newLocationUrl
      log.debug "HTTP status for '${ gradleDownloaderInfo.downloadSiteUrl }': ${ HttpStatus.MOVED_PERMANENTLY.reason }(${ HttpStatus.MOVED_PERMANENTLY.code }). New location is '${ newLocationUrl }'."
    }

    log.info "Downloading: '${ realDownloadUrl }' ==> '${ gradleDownloaderInfo.downloadTargetFileAbsolutePath }'."

    String contentLength = headOnlyHttpClient.toBlocking().exchange(HttpRequest.HEAD(realDownloadUrl)).header("Content-Length") ?: "1"
    log.debug "Content-Length for '${ realDownloadUrl }': ${ contentLength } (${ contentLength as Long / (1024 * 1024) } MiB)."

    // GraalVM notes: do not try rewriting this with withClosable {} since I didn't manage to make it work.
    try (BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(gradleDownloaderInfo.downloadTargetFileAbsolutePath), 1024 * 1024)) {
      Long downloadedBytesCount = 0
      streamingHttpClient.exchangeStream(HttpRequest.GET(realDownloadUrl).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE))
                         .map((HttpResponse<ByteBuffer<?>> byteBufferHttpResponse) -> {
                           byte[] byteArray = byteBufferHttpResponse.body.orElseThrow().toByteArray()
                           downloadedBytesCount += byteArray.length
                           printf("\rDownloading '${ realDownloadUrl }': %d%%", (downloadedBytesCount * 100 / (contentLength as Long)) as Long)

                           return byteArray
                         })
                          // GraalVM notes: use lambdas here since it I didn't succeed with closures.
                         .blockingSubscribe((byte[] byteArray) -> fileOutputStream.write(byteArray), Functions.ON_ERROR_MISSING, () -> println "")
    }

    return new File(gradleDownloaderInfo.downloadTargetFileAbsolutePath)
  }
}
