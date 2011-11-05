package de.is24.nexus.yum.plugin.integration;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.TestContainer;


public class CreateNewRpmRepositoryIT extends AbstractNexusTestBase {
  private static final String DUMMY_ARTIFACT = "dummy-rpm";
  private static final String GROUP_ID = "de.is24.test";
  private static final String ARTIFACT_VERSION_1 = "1.0";
  private static final String ARTIFACT_VERSION_2 = "2.0";
  private static final String NEW_REPO_ID = "next-try";
  private static final String REPO_XML = "<repository>" + "<data>" +
    "<contentResourceURI>%s/repositories/%s</contentResourceURI>" +
    "<id>%s</id>" + "<name>%s</name>" + "<provider>maven2</provider>" +
    "<providerRole>org.sonatype.nexus.proxy.repository.Repository</providerRole>" + "<format>maven2</format>" +
    "<repoType>hosted</repoType>" + "<exposed>true</exposed>" + "<writePolicy>ALLOW_WRITE</writePolicy>" +
    "<browseable>true</browseable>" + "<indexable>true</indexable>" + "<notFoundCacheTTL>1440</notFoundCacheTTL>" +
    "<repoPolicy>RELEASE</repoPolicy>" + "</data>" + "</repository>";

  static {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Test
  public void shouldCreateRepoRpmsForNewRpmsInNewRepository() throws Exception {
    givenTestRepository();

    Thread.sleep(5000);

    assertEquals(deployRpm(DUMMY_ARTIFACT, GROUP_ID, ARTIFACT_VERSION_1, NEW_REPO_ID), SC_CREATED);
    assertEquals(deployRpm(DUMMY_ARTIFACT, GROUP_ID, ARTIFACT_VERSION_2, NEW_REPO_ID), SC_CREATED);

    Thread.sleep(5000);

    // is24-rel-next-try-1.0-repo-1-1.noarch.rpm
    executeGet("/yum/repo/is24-rel-" + NEW_REPO_ID + "-" + ARTIFACT_VERSION_1 + "-repo-1-1.noarch.rpm");
    executeGet("/yum/repo/is24-rel-" + NEW_REPO_ID + "-" + ARTIFACT_VERSION_2 + "-repo-1-1.noarch.rpm");
  }

  private void givenTestRepository() throws AuthenticationException, IOException, UnsupportedEncodingException,
    ClientProtocolException {
    HttpResponse response = executeGetWithResponse("/repositories/" + NEW_REPO_ID);
    int statusCode = response.getStatusLine().getStatusCode();
    response.getEntity().getContent().close();
    if (statusCode != SC_OK) {
      response = executePost("/repositories", createRepositoryXml(NEW_REPO_ID));
      assertEquals(content(response), SC_CREATED, statusCode(response));
    }
  }

  private StringEntity createRepositoryXml(String repositoryId) throws UnsupportedEncodingException {
    StringEntity entity = new StringEntity(format(REPO_XML, SERVICE_BASE_URL, repositoryId, repositoryId,
        repositoryId));
    entity.setContentType("application/xml");
    return entity;
  }
}
