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
package org.sonatype.nexus.testsuite.nuget

import org.sonatype.nexus.blobstore.api.BlobStoreManager
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.http.HttpStatus

import com.google.common.io.Files
import groovy.util.slurpersupport.GPathResult
import org.apache.http.HttpResponse
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy
import org.ops4j.pax.exam.spi.reactors.PerClass

import static org.hamcrest.CoreMatchers.notNullValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.is
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.bytes
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status

/**
 * Test NuGet group repository behaviour
 */
@ExamReactorStrategy(PerClass.class)
class NugetGroupIT
    extends NugetProxyITSupport
{

  public static final String PROXY_REPO_NAME = 'test-nuget-proxy'

  public static final String HOSTED_REPO_NAME = 'test-nuget-hosted'

  public static final String GROUP_REPO_HOSTED_FIRST_NAME = 'test-nuget-group-hosted-first'

  public static final String GROUP_REPO_PROXY_FIRST_NAME = 'test-nuget-group-proxy-first'

  public static final String GROUP_REPO_PROXY_ONLY_NAME = 'test-nuget-group-proxy-only'

  NugetClient proxy

  NugetClient hosted

  NugetClient proxyFirstGroup

  NugetClient hostedFirstGroup

  NugetClient proxyOnlyGroup

  static final Map<String, String> SONATYPE_COMPONENT = [
      id     : 'SONATYPE.TEST',
      version: '1.0',
      nupkg  : 'SONATYPE.TEST.1.0.nupkg'
  ].asImmutable()

  @Before
  public void createRepositories() {
    //proxy repo
    configureProxyResources()
    final String remoteStorageUrl = proxyServer.getUrl().toExternalForm() + "/" + REMOTE_NUGET_REPO_PATH
    final Repository proxyRepo = createRepository(proxyConfig(PROXY_REPO_NAME, remoteStorageUrl))
    proxy = nugetClient(proxyRepo)

    //hosted repo
    final Repository hostedRepo = createRepository(hostedConfig(HOSTED_REPO_NAME))
    hosted = nugetClient(hostedRepo)

    //group repos
    final Repository groupRepoProxyFirst =
        createRepository(groupConfig(GROUP_REPO_PROXY_FIRST_NAME, PROXY_REPO_NAME, HOSTED_REPO_NAME))
    proxyFirstGroup = nugetClient(groupRepoProxyFirst)
    final Repository groupRepoHostedFirst =
        createRepository(groupConfig(GROUP_REPO_HOSTED_FIRST_NAME, HOSTED_REPO_NAME, PROXY_REPO_NAME))
    hostedFirstGroup = nugetClient(groupRepoHostedFirst)
    final Repository groupRepoProxyOnly =
        createRepository(groupConfig(GROUP_REPO_PROXY_ONLY_NAME, PROXY_REPO_NAME))
    proxyOnlyGroup = nugetClient(groupRepoProxyOnly)
  }

  @Test
  void 'Repository metadata is available from the group'() {
    final String repositoryMetadata = proxyOnlyGroup.getRepositoryMetadata()
    assertThat(repositoryMetadata, is(notNullValue()))
    assertThat(repositoryMetadata, containsString('<Schema Namespace="NuGetGallery"'))
  }

  @Test
  void 'Initially the group feed contains no data'() {
    assertThat(parseFeedXml(proxyOnlyGroup.vsSearchFeedXml()).size(), is(0))
  }

  @Test
  void 'Feed contains proxy repo data'() {
    proxy.vsSearchFeedXml('jQuery') // prime local from fake proxy server
    assertThat(parseFeedXml(proxyOnlyGroup.vsSearchFeedXml()).size(),
        is(VS_DEFAULT_PAGE_REQUEST_SIZE))
  }

  @Test
  void 'GET requests can download from proxy member of group'() {
    assertThat(bytes(proxyOnlyGroup.get("${SONATYPE_COMPONENT.id}/${SONATYPE_COMPONENT.version}")),
        is(expectedTestBytes()))
  }

  @Test
  void 'Visual Studio queries can download from proxy member of group'() {
    // TODO - KR proxy must contain the package first or vs queries do not work, expected or not?
    final HttpResponse proxyResponse = proxy.packageContent(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version)
    assertThat(status(proxyResponse), is(HttpStatus.OK))

    final byte[] proxyBytes = bytes(proxyResponse)
    assertThat(proxyBytes, is(expectedTestBytes()))

    final HttpResponse entry = proxyOnlyGroup.entry(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version)

    assertThat(status(entry), is(HttpStatus.OK))

    final HttpResponse response = proxyOnlyGroup.
        packageContent(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version)

    assertThat(status(response), is(HttpStatus.OK))
    assertThat(bytes(response), is(expectedTestBytes()))
  }

  @Test
  void 'Visual Studio count queries should work consistently'() {
    int proxyCount = proxy.vsCount()
    int hostedCount = hosted.vsCount()
    int groupCount = proxyFirstGroup.vsCount()

    // Ensure the count reflects what is remotely available
    assertThat('count', proxyCount, is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))
    assertThat('count', hostedCount, is(0))
    assertThat('count', groupCount, is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))

    final int publish = hosted.publish(resolveTestFile(SONATYPE_COMPONENT.nupkg))
    assertThat(publish, is(HttpStatus.CREATED))
    waitFor(calmPeriod())

    proxyCount = proxy.vsCount()
    hostedCount = hosted.vsCount()
    groupCount = proxyFirstGroup.vsCount()

    assertThat('count should be updated with published component', groupCount,
        is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT + 1))
    assertThat('count should be updated with published component', hostedCount, is(1))
    assertThat('count should not be affected', proxyCount, is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))
  }

  @Test
  void 'Visual Studio Package count queries should work consistently'() {
    int proxyCount = proxy.vsPackageCount()
    int hostedCount = hosted.vsPackageCount()
    int groupCount = proxyFirstGroup.vsPackageCount()

    // Ensure the count reflects what is remotely available
    assertThat('count', proxyCount, is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))
    assertThat('count', hostedCount, is(0))
    assertThat('count', groupCount, is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))

    final int publish = hosted.publish(resolveTestFile(SONATYPE_COMPONENT.nupkg))
    assertThat(publish, is(HttpStatus.CREATED))
    waitFor(calmPeriod())

    proxyCount = proxy.vsCount()
    hostedCount = hosted.vsCount()
    groupCount = proxyFirstGroup.vsCount()

    assertThat('count should be updated with published component', groupCount,
        is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT + 1))
    assertThat('count should be updated with published component', hostedCount, is(1))
    assertThat('count should not be affected', proxyCount, is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))
  }

  @Test
  void 'Visual Studio search queries should aggregate results across member repositories'() {
    final int publish = hosted.publish(resolveTestFile('SONATYPE.TEST.1.1.nupkg'))
    assertThat(publish, is(HttpStatus.CREATED))
    waitFor(calmPeriod())
    proxy.packageContent(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version) // prime our fake proxy cache

    assertThat(hosted.vsSearchCount(SONATYPE_COMPONENT.id), is(1))
    assertThat(proxy.vsSearchCount(SONATYPE_COMPONENT.id), is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT))
    assertThat(proxyFirstGroup.vsSearchCount(SONATYPE_COMPONENT.id), is(VISUAL_STUDIO_INITIAL_ALL_PACKAGES_COUNT + 1))

    List<Map<String, String>> hostedSearch = parseFeedXml(hosted.vsSearchFeedXml(SONATYPE_COMPONENT.id))
    List<Map<String, String>> proxySearch = parseFeedXml(proxy.vsSearchFeedXml(SONATYPE_COMPONENT.id))
    List<Map<String, String>> groupSearch = parseFeedXml(proxyFirstGroup.vsSearchFeedXml(SONATYPE_COMPONENT.id))

    assertThat(hostedSearch.size(), is(1))
    assertThat(hostedSearch[0].VERSION, is('1.1'))
    assertThat(proxySearch.size(), is(1))
    assertThat(proxySearch[0].VERSION, is('1.0'))
    assertThat(groupSearch.size(), is(2))
    assertThat(groupSearch.collect { it.VERSION }, containsInAnyOrder('1.1', '1.0'))
    // fails due to https://issues.sonatype.org/browse/NEXUS-9005
//    assertThat(groupSearch.collect { it.ISLATESTVERSION }, containsInAnyOrder('true', 'false'))
  }
  
  @Test
  @Ignore("https://issues.sonatype.org/browse/NEXUS-8989") // ignored until this issue is resolved
  void 'Order of group member repositories is respected'() {
    // make same component available from both proxy and hosted repo
    final HttpResponse proxyResponse = proxy.packageContent(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version)
    assertThat(status(proxyResponse), is(HttpStatus.OK))
    hosted.publish(resolveTestFile(SONATYPE_COMPONENT.nupkg))

    XmlSlurper xmlSlurper = new XmlSlurper()
    final GPathResult proxyEntry = xmlSlurper.parseText(
        proxy.entryXml(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version))
    final GPathResult hostedEntry = xmlSlurper.parseText(
        hosted.entryXml(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version))
    final GPathResult proxyFirstEntry = xmlSlurper.parseText(
        proxyFirstGroup.entryXml(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version))
    final GPathResult hostedFirstEntry = xmlSlurper.parseText(
        hostedFirstGroup.entryXml(SONATYPE_COMPONENT.id, SONATYPE_COMPONENT.version))

    // compare updated value to see if we're getting the expected feed based on group member ordering
    assertThat('proxy repo content should be returned when proxy is first in group', proxyEntry.updated.text(),
        is(proxyFirstEntry.updated.text()))
    // TODO - KR presently failing as repo order is not respected
    assertThat('hosted repo content should be returned when hosted is first in group', hostedEntry.updated.text(),
        is(hostedFirstEntry.updated.text()))
  }

  private byte[] expectedTestBytes(String file = 'SONATYPE.TEST.1.0.nupkg') {
    return Files.toByteArray(resolveTestFile(file))
  }

  private Configuration groupConfig(final String name, String... members) {
    return new Configuration(
        repositoryName: name,
        recipeName: 'nuget-group',
        online: true,
        attributes:
            [
                group  : [
                    memberNames: members.toList()
                ]
                ,
                storage: [
                    blobStoreName: BlobStoreManager.DEFAULT_BLOBSTORE_NAME
                ]
            ]
    )
  }
}
