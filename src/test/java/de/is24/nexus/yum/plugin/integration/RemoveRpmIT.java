package de.is24.nexus.yum.plugin.integration;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.junit.Test;

public class RemoveRpmIT extends AbstractNexusTestBase {

  private static final String NEW_REPO_ID = "remove-test-repo";
  private static final String GROUP_ID = "test";
  private static final String ARTIFACT_VERSION = "0.0.1-TEST";
  private static final String DUMMY_ARTIFACT = "dummy-artifact-foo";

  @Test
  public void shouldRemoveRpmFromYumRepoIfRemovedByWebGui() throws Exception {
    givenTestRepository(NEW_REPO_ID);
    wait(5, SECONDS);
    assertEquals(deployRpm(DUMMY_ARTIFACT, GROUP_ID, ARTIFACT_VERSION, NEW_REPO_ID), SC_CREATED);
    wait(5, SECONDS);
    executeDelete("/repositories/" + NEW_REPO_ID + "/content/" + GROUP_ID);
    wait(25, SECONDS);
    String primaryXml = gzipResponseContent(executeGetWithResponse(NEXUS_BASE_URL + "/content/repositories/" + NEW_REPO_ID
        + "/repodata/primary.xml.gz"));
    assertThat(primaryXml, not(containsString(DUMMY_ARTIFACT)));
  }

  private void executeDelete(String url) throws AuthenticationException, UnsupportedEncodingException, IOException,
      ClientProtocolException {
    HttpDelete request = new HttpDelete(SERVICE_BASE_URL + url);
    setCredentials(request);
    HttpResponse response = client.execute(request);
    consume(response.getEntity());
    assertEquals(SC_NO_CONTENT, statusCode(response));
  }
}
