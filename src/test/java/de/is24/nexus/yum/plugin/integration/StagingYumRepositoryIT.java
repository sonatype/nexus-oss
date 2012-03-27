package de.is24.nexus.yum.plugin.integration;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StagingYumRepositoryIT extends AbstractNexusTestBase {
  private static final String DUMMY_ARTIFACT_ID = "dummy-artifakt";
  private static final String DUMMY_GROUP_ID = "de.is24.yum.group.test";
  private static final String DUMMY_VERSION = "1.4.5";
  private static final String STAGING_PROFILE_ID = "124c925916d85479"; // see
                                                                       // staging.xml
  private static final Logger LOG = LoggerFactory.getLogger(StagingYumRepositoryIT.class);
  private static final String PROMOTE_REQUEST =
 "<?xml version=\"1.0\" encoding=\"UTF-8\"?><promoteRequest><data><stagedRepositoryId>%s</stagedRepositoryId><targetRepositoryId>production</targetRepositoryId><description>Dummy Description</description></data></promoteRequest>";

  @Test
  public void shouldStageRpmWithoutOverridingMetaData() throws Exception {
    givenUploadedRpmToStaging(DUMMY_GROUP_ID, DUMMY_ARTIFACT_ID, DUMMY_VERSION);
    wait(2, SECONDS);
    final String repositoryId = getStagingRepositoryId(STAGING_PROFILE_ID);
    givenClosedStagingRepo(repositoryId);
    wait(2, SECONDS);
    whenUserPromotesStagingRepo(repositoryId);

    // wait until yum-repository is really created
    wait(2, SECONDS);
    thenEnsureThatRepositoryMetadataDoesNotContainOldUrl(repositoryId);
  }

  private void thenEnsureThatRepositoryMetadataDoesNotContainOldUrl(String repositoryId) throws AuthenticationException, IOException {
    HttpResponse response = executeGetWithResponse(NEXUS_BASE_URL +
      "/content/repositories/production/repodata/primary.xml.gz");
    assertEquals(SC_OK, statusCode(response));

    String content = gzipResponseContent(response);
    LOG.info("Content of primary.xml : {}", content);
    assertThat(content, not(containsString(repositoryId)));
    assertThat(content, containsString("test-rpm-1.45.rpm"));
    assertThat(content, containsString("dummy-artifakt-1.4.5.rpm"));
  }

  private void whenUserPromotesStagingRepo(String repositoryId) throws Exception {
    HttpResponse response = executePost("/staging/profiles/" + STAGING_PROFILE_ID + "/promote",
        new StringEntity(format(PROMOTE_REQUEST, repositoryId)),
      new BasicHeader("Content-Type", "application/xml"));
    System.out.println(EntityUtils.toString(response.getEntity()));
    assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());
    consume(response.getEntity());
  }

  private void givenClosedStagingRepo(String repositoryId) throws AuthenticationException, IOException {
    HttpResponse response = executePost("/staging/profiles/" + STAGING_PROFILE_ID + "/finish",
        new StringEntity(format(PROMOTE_REQUEST, repositoryId)),
      new BasicHeader("Content-Type", "application/xml"));
    assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());
    consume(response.getEntity());
  }
}
