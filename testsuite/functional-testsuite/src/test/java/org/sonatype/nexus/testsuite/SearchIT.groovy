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
package org.sonatype.nexus.testsuite

import javax.inject.Inject

import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.http.HttpStatus
import org.sonatype.nexus.repository.manager.RepositoryManager
import org.sonatype.nexus.repository.maven.policy.VersionPolicy
import org.sonatype.nexus.repository.storage.WritePolicy

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.junit.Before
import org.junit.Test
import org.ops4j.pax.exam.Option
import org.ops4j.pax.exam.OptionUtils
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.ops4j.pax.exam.CoreOptions.maven
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle

/**
 * Search related Siesta tests.
 */
class SearchIT
extends FunctionalTestSupport
{

  @Inject
  private RepositoryManager repositoryManager

  SearchIT(final String executable, final String[] options) {
    super(executable, options)
  }

  @org.ops4j.pax.exam.Configuration
  public static Option[] config() {
    OptionUtils.combine(
        FunctionalTestSupport.config(),
        wrappedBundle(maven("org.apache.httpcomponents", "httpmime").versionAsInProject())
            .overwriteManifest(WrappedUrlProvisionOption.OverwriteMode.FULL).instructions("DynamicImport-Package=*")
    )
  }

  @Before
  void setup() {
    CloseableHttpClient client = httpClient()
    setupMaven(client)
    setupNuget(client)
    setupRaw(client)
    waitFor(calmPeriod())
  }

  CloseableHttpClient httpClient() {
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider()
    credentialsProvider.setCredentials(
        new AuthScope(nexusUrl.host, -1), new UsernamePasswordCredentials('admin', 'admin123')
    )
    HttpClientBuilder builder = HttpClients.custom()
    builder.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT).build())
    builder.setDefaultCredentialsProvider(credentialsProvider)
    return builder.build()
  }

  void setupMaven(final CloseableHttpClient httpClient) {
    Repository repository = repositoryManager.create(new Configuration(
        repositoryName: 'search-test-maven',
        recipeName: 'maven2-hosted',
        online: true,
        attributes: [
            maven: [
                versionPolicy: VersionPolicy.RELEASE
            ],
            storage: [
                blobStoreName: BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
                writePolicy: WritePolicy.ALLOW_ONCE
            ]
        ]
    ))
    publish(httpClient, repository, resolveTestFile('aopalliance-1.0.jar'), 'aopalliance/aopalliance/1.0/aopalliance-1.0.jar')
  }

  void setupNuget(final CloseableHttpClient httpClient) {
    Repository repository = repositoryManager.create(new Configuration(
        repositoryName: 'search-test-nuget',
        recipeName: 'nuget-hosted',
        online: true,
        attributes: [
            storage: [
                blobStoreName: BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
                writePolicy: WritePolicy.ALLOW
            ]
        ]
    ))
    publish(httpClient, repository, resolveTestFile('SONATYPE.TEST.1.0.nupkg'), '')
  }

  void setupRaw(final CloseableHttpClient httpClient) {
    Repository repository = repositoryManager.create(new Configuration(
        repositoryName: 'search-test-raw',
        recipeName: 'raw-hosted',
        online: true,
        attributes: [
            storage: [
                blobStoreName: BlobStoreManager.DEFAULT_BLOBSTORE_NAME,
                writePolicy: WritePolicy.ALLOW
            ]
        ]
    ))
    publish(httpClient, repository, resolveTestFile('alphabet.txt'), 'alphabet.txt')
  }

  void publish(final CloseableHttpClient httpClient, final Repository repository, final File file, final String path) {
    HttpPut put = new HttpPut(repositoryBaseUrl(repository).resolve(path))

    MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create()
    reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
    reqEntity.addPart("file", new FileBody(file))

    put.setEntity(reqEntity.build())

    CloseableHttpResponse response = httpClient.execute(put)
    assertThat(response.statusLine.statusCode, is(HttpStatus.CREATED))
  }

  URI repositoryBaseUrl(final Repository repository) {
    return resolveUrl(nexusUrl, "/repository/${repository.name}/").toURI()
  }

  @Test
  void suite() {
    run("search/.*\\.t.js")
  }
}
