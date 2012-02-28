package de.is24.nexus.yum.plugin.integration;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.createDummyRpm;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.maven.cli.MavenCli;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.is24.nexus.yum.repository.utils.RepositoryTestUtils;


@Ignore("We need a valid licence for that")
public class StagingYumRepositoryIT extends AbstractNexusTestBase {
  private static final String STAGING_REPO_ID = "staging-test-profile-001";
  private static final String DUMMY_ARTIFACT_ID = "dummy-artifakt";
  private static final String DUMMY_GROUP_ID = "de.is24.staging.test";
  private static final String DUMMY_VERSION = "1.4.5";
  private static final String STAGING_PROFILE_ID = "124c925916d85479"; // see
                                                                       // staging.xml
  private static final Logger LOG = LoggerFactory.getLogger(StagingYumRepositoryIT.class);
  private static final String PROMOTE_REQUEST =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><promoteRequest><data><stagedRepositoryId>" +
    STAGING_REPO_ID +
    "</stagedRepositoryId><targetRepositoryId>production</targetRepositoryId><description>Dummy Description</description></data></promoteRequest>";

  private final MavenCli maven = new MavenCli();

  @Test
  public void testtest() throws Exception {
    RepositoryTestUtils.createDummyRpm("test-rpm", "1.45", new File("target"));
  }

  @Test
  public void shouldStageRpmWithoutOverridingMetaData() throws Exception {
    givenUploadedArtifact();
    wait(2, SECONDS);
    givenClosedStagingRepo();
    wait(2, SECONDS);
    whenUserPromotesStagingRepo();

    // wait until yum-repository is really created
    wait(2, SECONDS);
    thenEnsureThatRepositoryMetadataDoesNotContainOldUrl();
  }

  private void thenEnsureThatRepositoryMetadataDoesNotContainOldUrl() throws AuthenticationException, IOException {
    HttpResponse response = executeGetWithResponse(NEXUS_BASE_URL +
      "/content/repositories/production/repodata/primary.xml.gz");
    assertEquals(SC_OK, statusCode(response));

    String content = gzipResponseContent(response);
    LOG.info("Content of primary.xml : {}", content);
    assertThat(content, not(containsString(STAGING_REPO_ID)));
    assertThat(content, containsString("test-rpm-1.45.rpm"));
    assertThat(content, containsString("dummy-artifakt-1.4.5.rpm"));
  }

  private void whenUserPromotesStagingRepo() throws Exception {
    HttpResponse response = executePost("/staging/profiles/" + STAGING_PROFILE_ID + "/promote",
      new StringEntity(PROMOTE_REQUEST),
      new BasicHeader("Content-Type", "application/xml"));
    System.out.println(EntityUtils.toString(response.getEntity()));
    assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());
    consume(response.getEntity());
  }

  private void givenClosedStagingRepo() throws AuthenticationException, IOException {
    HttpResponse response = executePost("/staging/profiles/" + STAGING_PROFILE_ID + "/finish",
      new StringEntity(PROMOTE_REQUEST),
      new BasicHeader("Content-Type", "application/xml"));
    assertEquals(SC_CREATED, response.getStatusLine().getStatusCode());
    consume(response.getEntity());
  }

  private void givenUploadedArtifact() throws NoSuchAlgorithmException, IOException {
    File rpmFile = createDummyRpm(DUMMY_ARTIFACT_ID, DUMMY_VERSION);
    executeMaven("deploy:deploy-file", "-Dfile=" + rpmFile.getAbsolutePath(),
      "-Durl=" + SERVICE_BASE_URL + "/staging/deploy/maven2", "-DgroupId=" + DUMMY_GROUP_ID,
      "-DartifactId=" + DUMMY_ARTIFACT_ID,
      "-Dversion=" + DUMMY_VERSION, "-Dpackaging=rpm",
      "-X",
      "-DrepositoryId=local-nexus", "-s",
      "../src/test/resources/maven/settings.xml");
  }

  private void executeMaven(String... params) {
    int exitCode = maven.doMain(params, "target", null, null);

    if (exitCode != 0) {
      throw new RuntimeException("Maven ended with exit code " + exitCode);
    }
  }
}
