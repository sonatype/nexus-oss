/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.nuget;


import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple NuGet client for ITs.
 */
public class NugetClient
    extends ComponentSupport
{
  public static final String VS_SEARCH_COUNT_TEMPLATE = "Search()/$count?$filter=IsAbsoluteLatestVersion&searchTerm='%s'&targetFramework='net45'&includePrerelease=true";

  public static final String VS_SEARCH_FEED_TEMPLATE = "Search()?$filter=IsAbsoluteLatestVersion&$skip=0&$top=30&searchTerm='%s'&targetFramework='net45'&includePrerelease=true";

  private final HttpClient httpClient;

  private final HttpClientContext httpClientContext;

  private final URI repositoryBaseUri;

  public NugetClient(final HttpClient httpClient,
                     final HttpClientContext httpClientContext,
                     final URI repositoryBaseUri)
  {
    this.httpClient = checkNotNull(httpClient);
    this.httpClientContext = checkNotNull(httpClientContext);
    this.repositoryBaseUri = checkNotNull(repositoryBaseUri);

    checkArgument(repositoryBaseUri.toString().endsWith("/"));
  }

  public String getRepositoryMetadata() throws Exception {
    return asString(get("$metadata"));
  }

  /**
   * Publishes a file to the nuget repository
   *
   * @return the HTTP status code
   */
  public int publish(final File file) throws Exception {
    checkNotNull(file);
    HttpPut put = new HttpPut(repositoryBaseUri);

    MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
    reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
    reqEntity.addPart("package", new FileBody(file));

    put.setEntity(reqEntity.build());

    final HttpResponse execute = httpClient.execute(put, httpClientContext);

    return execute.getStatusLine().getStatusCode();
  }

  public String feedXml(final String query) throws IOException {
    return asString(get(query));
  }

  public int count(final String query) throws IOException {
    final String s = asString(get(query));
    return Integer.parseInt(s);
  }

  public HttpResponse entry(final String packageId, final String version) throws IOException {
    return get(String.format("Packages(Id='%s',Version='%s')", packageId, version));
  }

  public String entryXml(final String packageId, final String version) throws IOException {
    return asString(entry(packageId, version));
  }

  public String vsSearchFeedXml(final String searchTerm) throws IOException {
    return feedXml(String.format(VS_SEARCH_FEED_TEMPLATE, searchTerm));
  }

  public int vsSearchCount(final String searchTerm) throws IOException {
    return count(String.format(VS_SEARCH_COUNT_TEMPLATE, searchTerm));
  }

  /**
   * Issues a delete request to the NuGet repository.
   *
   * @return HTTP status code
   */
  public int delete(final String packageId, final String version) throws IOException {
    final URI deleteURI = repositoryBaseUri.resolve(String.format("%s/%s", packageId, version));
    final HttpDelete delete = new HttpDelete(deleteURI);
    final HttpResponse response = execute(delete);
    return response.getStatusLine().getStatusCode();
  }

  private String asString(final HttpResponse response) throws IOException {
    assert response.getStatusLine().getStatusCode() == HttpStatus.OK;
    final String asString = EntityUtils.toString(response.getEntity());

    String synopsis = asString.substring(0, Math.min(asString.length(), 60));
    synopsis = synopsis.replaceAll("\\n", "");
    log.info("Nuget client received {}", synopsis);

    return asString;
  }

  /**
   * GET a response from the repository.
   */
  private HttpResponse get(final String path) throws IOException {
    final HttpGet get = new HttpGet(repositoryBaseUri.resolve(path));
    return execute(get);
  }

  private HttpResponse execute(final HttpUriRequest request) throws IOException {
    log.info("Nuget client requesting {}", request);
    final HttpResponse response = httpClient.execute(request, httpClientContext);
    log.info("Nuget client received {}", response);
    return response;
  }


}
