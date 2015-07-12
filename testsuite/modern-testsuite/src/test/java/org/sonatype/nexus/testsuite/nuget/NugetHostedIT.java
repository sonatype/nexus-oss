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
package org.sonatype.nexus.testsuite.nuget;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import com.google.common.io.Files;
import org.apache.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.bytes;
import static org.sonatype.nexus.testsuite.repository.FormatClientSupport.status;

/**
 * Tests NuGet hosted repositories.
 */
@ExamReactorStrategy(PerClass.class)
public class NugetHostedIT
    extends NugetITSupport
{

  private static final String HOSTED_REPO = "nuget-test-hosted";

  private NugetClient nuget;

  @Before
  public void createHostedRepository() throws Exception {
    final Configuration config = hostedConfig(HOSTED_REPO);

    final Repository repository = createRepository(config);

    nuget = nugetClient(repository);
  }

  /**
   * Simple smoke test to ensure a hosted repo is actually reachable.
   */
  @Test
  public void hostedRepositoryIsAvailable() throws Exception {
    final String repositoryMetadata = nuget.getRepositoryMetadata();
    assertThat(repositoryMetadata, is(notNullValue()));
    assertThat(repositoryMetadata, containsString("<Schema Namespace=\"NuGetGallery\""));
  }

  /**
   * Push a package, ensure it turns up in Visual Studio's default feed, then delete it and ensure it's gone.
   */
  @Test
  public void putFindDeletePackage() throws Exception {
    final int publish = nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));
    assertThat(publish, is(HttpStatus.CREATED));
    waitFor(calmPeriod());

    // Visual Studio-style queries
    int count = nuget.count(VISUAL_STUDIO_INITIAL_COUNT_QUERY);
    assertThat("count", count, is(1));

    String feed = nuget.feedXml(VISUAL_STUDIO_INITIAL_FEED_QUERY);
    final List<Map<String, String>> entries = parseFeedXml(feed);

    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));

    assertThat(status(nuget.delete("SONATYPE.TEST", "1.0")), is(HttpStatus.NO_CONTENT));

    // Now ensure that the item has been deleted
    assertThat("count after deletion", nuget.count(VISUAL_STUDIO_INITIAL_COUNT_QUERY), is(0));

    final List<Map<String, String>> remainingEntries = parseFeedXml(nuget.feedXml(VISUAL_STUDIO_INITIAL_FEED_QUERY));
    assertThat("entries after deletion", remainingEntries.size(), is(0));
  }


  /**
   * Ensure an uploaded package shows up in its single-package feed.
   */
  @Test
  public void entryForPackage() throws Exception {
    nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));

    final String entryXml = nuget.entryXml("SONATYPE.TEST", "1.0");

    final List<Map<String, String>> entries = parseFeedXml(entryXml);

    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));
  }

  /**
   * Requesting the entry for a nonexistent package should return 404.
   */
  @Test
  public void entryForMissingPackageReturns404() throws Exception {
    final HttpResponse response = nuget.entry("no-such-package", "1.0");
    assertThat(status(response), is(HttpStatus.NOT_FOUND));
  }

  /**
   * Deleting a nonexistent package should return 404.
   */
  @Test
  public void deleteMissingPackageReturns404() throws Exception {
    final int delete = status(nuget.delete("no-such-package", "1.0"));
    assertThat(delete, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void downloadPackage() throws Exception {
    final int publish = nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));
    assertThat(publish, is(HttpStatus.CREATED));
    waitFor(calmPeriod());

    final HttpResponse response = nuget.packageContent("SONATYPE.TEST", "1.0");

    assertThat(status(response), is(HttpStatus.OK));
    assertThat(bytes(response), is(Files.toByteArray(resolveTestFile("SONATYPE.TEST.1.0.nupkg"))));
  }

  /**
   * Search for an uploaded package with a search term.
   */
  @Test
  public void searchForPackage() throws Exception {
    nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));

    final String searchTerm = "SONATYPE.TEST";
    final int count = nuget.vsSearchCount(searchTerm);

    assertThat("count", count, is(1));

    final String feedXml = nuget.vsSearchFeedXml(searchTerm);

    final List<Map<String, String>> entries = parseFeedXml(feedXml);
    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is(searchTerm));
  }

  /**
   * Ensure that Visual Studio's 'specific packages'-style ODATA queries can find an uploaded test package.
   */
  @Test
  public void visualStudioSearchForUpdates() throws Exception {
    nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));

    final String feedXml = nuget.feedXml("Packages()?$filter=((((tolower(Id)%20eq%20'sonatype.test')" +
        "%20or%20(tolower(Id)%20eq%20'51degrees.mobi'))" +
        "%20or%20(tolower(Id)%20eq%20'microsoft.aspnet.razor'))" +
        "%20or%20(tolower(Id)%20eq%20'microsoft.aspnet.webpages'))" +
        "%20or%20(tolower(Id)%20eq%20'microsoft.aspnet.mvc')" +
        "&$orderby=Id");

    final List<Map<String, String>> entries = parseFeedXml(feedXml);
    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));
  }

  /**
   * Ensure that a url encoded ODATA query for an uploaded package's metadata
   * returns an entry that contains a valid content link to the package.
   */
  @Test
  public void encodedOdataQuery() throws Exception {
    nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));

    final String entryXml = nuget.feedXml(URLEncoder.encode("Packages(Id='SONATYPE.TEST',Version='1.0')", "UTF-8"));
    final List<Map<String, String>> entries = parseFeedXml(entryXml);

    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));

    final String contentSrc = entries.get(0).get("LOCATION");
    final HttpResponse response = nuget.get(contentSrc);
    assertThat(String.format("%n%s", contentSrc), status(response), is(HttpStatus.OK));
  }
}
