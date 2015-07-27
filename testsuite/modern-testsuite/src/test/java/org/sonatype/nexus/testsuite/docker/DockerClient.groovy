/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.docker

import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.docker.RepositoryReverseProxyFacet
import org.sonatype.nexus.testsuite.repository.FormatClientSupport
import org.sonatype.sisu.litmus.testsupport.TestData

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Charsets
import org.apache.commons.io.FileUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.FileEntity
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils

import static org.sonatype.nexus.repository.http.HttpStatus.NOT_FOUND
import static org.sonatype.nexus.repository.http.HttpStatus.NO_CONTENT
import static org.sonatype.nexus.repository.http.HttpStatus.OK
import static org.sonatype.nexus.repository.view.ContentTypes.APPLICATION_JSON
import static org.sonatype.nexus.repository.view.ContentTypes.APPLICATION_OCTET_STREAM

/**
 * Docker Client.
 */
class DockerClient
    extends FormatClientSupport
{
  private static final ObjectMapper mapper = new ObjectMapper()

  private TestData testData

  DockerClient(final HttpClient httpClient,
               final HttpClientContext httpClientContext,
               final Repository repository,
               final TestData testData)
  {
    super(httpClient, httpClientContext, dockerUri(repository))
    this.testData = testData
  }

  void push(final String name, String tag = 'latest') {
    def response = consume(put(
        repositoryBaseUri.toASCIIString(),
        "/v1/repositories/${name}/",
        "/v1/repositories/${name}/images/${tag}",
        APPLICATION_JSON
    ))
    assert status(response) == OK
    def endpoint = response.getFirstHeader('X-Docker-Endpoints')?.value
    assert endpoint
    endpoint = "http://" + endpoint

    def images = readImages("/v1/repositories/${name}/images/${tag}")
    def imagesToPush = []
    images.each { id ->
      if (status(consume(getIt(endpoint, "/v1/images/${id}/json"))) == NOT_FOUND) {
        imagesToPush << id
      }
    }
    imagesToPush.each { id ->
      assert consume(put(endpoint, "/v1/images/${id}/json", APPLICATION_JSON))?.statusLine?.statusCode == OK
      assert consume(put(endpoint, "/v1/images/${id}/layer", APPLICATION_OCTET_STREAM))?.statusLine?.statusCode == OK
      assert consume(putChecksum(endpoint, "/v1/images/${id}/checksum"))?.statusLine?.statusCode == OK
    }

    assert status(consume(put(
        endpoint,
        "/v1/repositories/${name}/tags/${tag}",
        APPLICATION_JSON
    ))) == OK

    assert status(consume(put(
        repositoryBaseUri.toASCIIString(),
        "/v1/repositories/${name}/images",
        new StringEntity('[]', APPLICATION_JSON, Charsets.UTF_8.name())
    ))) == NO_CONTENT
  }

  private HttpResponse put(String baseUri, String path, String contentType) {
    return put(baseUri, path, path, contentType)
  }

  private HttpResponse put(String baseUri, String path, String filePath, String contentType) {
    return put(baseUri, path, new FileEntity(testData.resolveFile(filePath), contentType))
  }

  private HttpResponse put(String baseUri, String path, HttpEntity entity) {
    HttpPut put = new HttpPut(new URI("${baseUri}${path}"))
    put.setEntity(entity)
    return execute(put)
  }

  private HttpResponse putChecksum(String baseUri, String path) {
    HttpPut put = new HttpPut(new URI("${baseUri}${path}"))
    put.addHeader('X-Docker-Checksum-Payload', FileUtils.readFileToString(testData.resolveFile(path)).trim())
    return execute(put)
  }

  private HttpResponse getIt(String baseUri, String path) {
    HttpGet get = new HttpGet(new URI("${baseUri}${path}"))
    return execute(get)
  }

  private List<String> readImages(final String path) {
    def imagesFile = testData.resolveFile(path)
    TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
    def images = mapper.readValue(imagesFile, typeRef)
    return images.collect { image ->
      image['id'] as String
    }
  }

  private static HttpResponse consume(final HttpResponse response) {
    EntityUtils.consume(response.entity)
    return response
  }

  private static URI dockerUri(final Repository repository) {
    def port = repository.facet(RepositoryReverseProxyFacet).getPort()
    return new URI("http://localhost:${port}")
  }

}
