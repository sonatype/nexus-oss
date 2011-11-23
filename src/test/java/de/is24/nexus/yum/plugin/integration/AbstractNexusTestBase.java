package de.is24.nexus.yum.plugin.integration;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.createDummyRpm;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;


public class AbstractNexusTestBase {
  protected static final String NEXUS_BASE_URL = "http://localhost:8080/nexus";
  protected static final String SERVICE_BASE_URL = NEXUS_BASE_URL + "/service/local";

  protected HttpClient client = new DefaultHttpClient();

  protected HttpResponse executePost(String url, HttpEntity entity, Header... headers) throws AuthenticationException,
    UnsupportedEncodingException, IOException, ClientProtocolException {
    HttpPost request = new HttpPost(SERVICE_BASE_URL + url);
    if (headers != null) {
      for (Header header : headers) {
        request.addHeader(header);
      }
    }
    setCredentials(request);
    request.setEntity(entity);
    return client.execute(request);
  }

  protected HttpResponse executePut(String url, HttpEntity entity) throws AuthenticationException,
    UnsupportedEncodingException, IOException, ClientProtocolException {
    HttpPut request = new HttpPut(SERVICE_BASE_URL + url);
    setCredentials(request);
    request.setEntity(entity);
    return client.execute(request);
  }

  protected void setCredentials(HttpRequest request) throws AuthenticationException {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials("admin", "admin123");
    request.addHeader(new BasicScheme().authenticate(creds, request));
  }

  protected String executeGet(String url) throws AuthenticationException, IOException {
    HttpResponse response = executeGetWithResponse(url);
    assertEquals(SC_OK, statusCode(response));
    return EntityUtils.toString(response.getEntity());
  }

  protected HttpResponse executeGetWithResponse(String url) throws AuthenticationException, IOException {
    if (!url.startsWith("http")) {
      url = SERVICE_BASE_URL + url;
    }

    HttpGet request = new HttpGet(url);
    setCredentials(request);
    return client.execute(request);
  }

	protected HttpResponse executeDeleteWithResponse(String url) throws AuthenticationException, IOException {
		if (!url.startsWith("http")) {
			url = SERVICE_BASE_URL + url;
		}

		HttpDelete request = new HttpDelete(url);
		setCredentials(request);
		return client.execute(request);
	}

  protected int statusCode(HttpResponse response) {
    return response.getStatusLine().getStatusCode();
  }

  protected String content(HttpResponse response) throws IOException {
    return IOUtils.toString(response.getEntity().getContent());
  }

  protected int deployRpm(File rpmFile, String artifactId, String groupId, String version, String repositoryId)
    throws HttpException, IOException {
    AbstractNexusIntegrationTest testMock = createMock(AbstractNexusIntegrationTest.class);
    replay(testMock);

    Gav gav = new Gav(groupId, artifactId, version, null, "rpm", null, null, null, false, null, false, null);
    return new DeployUtils(testMock).deployUsingGavWithRest(repositoryId, gav, rpmFile);
  }

  protected int deployRpm(String artifactId, String groupId, String version, String repositoryId)
    throws NoSuchAlgorithmException, IOException {
    return deployRpm(createDummyRpm(artifactId, version), artifactId, groupId, version, repositoryId);
  }

}
