package org.sonatype.nexus.testsuite.nuget;

import java.util.List;
import java.util.Map;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpStatus;

import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests NuGet hosted repositories.
 *
 * @since 3.0
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

    final Repository repository = createOrReplaceRepository(config);

    nuget = nugetClient(repository);
  }

  @After
  public void deleteHostedRepository() throws Exception {
    deleteRepository(HOSTED_REPO);
  }

  @Test
  public void hostedRepositoryIsAvailable() throws Exception {
    final String repositoryMetadata = nuget.getRepositoryMetadata();
    assertThat(repositoryMetadata, is(notNullValue()));
    assertThat(repositoryMetadata.contains("<Schema Namespace=\"NuGetGallery\""), is(true));
  }

  @Test
  public void putFindDeletePackage() throws Exception {
    final int publish = nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));
    assertThat(publish, is(HttpStatus.CREATED));
    waitFor(calmPeriod());

    // Visual Studio-style queries
    int count = nuget
        .count("Search()/$count?$filter=IsLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=false");

    assertThat("count", count, is(1));

    String feed = nuget.feedXml(
        "Search()?$filter=IsLatestVersion&$orderby=DownloadCount%20desc,Id&$skip=0&$top=30&searchTerm=''&targetFramework='net45'&includePrerelease=false");

    final List<Map<String, String>> entries = parseFeedXml(feed);

    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));

    final int delete = nuget.delete("SONATYPE.TEST", "1.0");
    assertThat(delete, is(HttpStatus.NO_CONTENT));
  }

  @Test
  public void entryForPackage() throws Exception {
    nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));

    final String entryXml = nuget.entryXml("SONATYPE.TEST", "1.0");

    final List<Map<String, String>> entries = parseFeedXml(entryXml);

    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));
  }

  @Test
  public void entryForMissingPackageReturns404() throws Exception {
    final HttpResponse response = nuget.entry("no-such-package", "1.0");
    assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void deleteMissingPackageReturns404() throws Exception {
    final int delete = nuget.delete("no-such-package", "1.0");
    assertThat(delete, is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void searchForPackage() throws Exception {
    nuget.publish(resolveTestFile("SONATYPE.TEST.1.0.nupkg"));

    final String feedXml = nuget.feedXml(
        "Search()/$count?$filter=IsAbsoluteLatestVersion&searchTerm='SONATYPE.TEST'&targetFramework='net45'&includePrerelease=true");

    final List<Map<String, String>> entries = parseFeedXml(feedXml);
    assertThat("entry count", entries.size(), is(1));
    assertThat("entry ID", entries.get(0).get("ID"), is("SONATYPE.TEST"));
  }

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
}
