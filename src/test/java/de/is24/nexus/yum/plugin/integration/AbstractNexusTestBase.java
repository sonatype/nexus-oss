package de.is24.nexus.yum.plugin.integration;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.createDummyRpm;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.http.util.EntityUtils.consume;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.index.artifact.Gav;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.test.utils.DeployUtils;

public class AbstractNexusTestBase {
  private static final String NEXUS_PASSWORD = "admin123";
  private static final String NEXUS_USERNAME = "admin";
  protected static final Logger LOG = LoggerFactory.getLogger(AbstractNexusTestBase.class);
  protected static final String NEXUS_BASE_URL = "http://localhost:8080/nexus";
  protected static final String SERVICE_BASE_URL = NEXUS_BASE_URL + "/service/local";
  private static final String REPO_XML = "<repository>" + "<data>" + "<contentResourceURI>%s/repositories/%s</contentResourceURI>"
      + "<id>%s</id>" + "<name>%s</name>" + "<provider>maven2</provider>"
      + "<providerRole>org.sonatype.nexus.proxy.repository.Repository</providerRole>" + "<format>maven2</format>"
      + "<repoType>hosted</repoType>" + "<exposed>true</exposed>" + "<writePolicy>ALLOW_WRITE</writePolicy>"
      + "<browseable>true</browseable>" + "<indexable>true</indexable>" + "<notFoundCacheTTL>1440</notFoundCacheTTL>"
      + "<repoPolicy>RELEASE</repoPolicy>" + "</data>" + "</repository>";

  protected HttpClient client = new DefaultHttpClient();

  protected HttpResponse executePost(String url, HttpEntity entity, Header... headers) throws AuthenticationException,
      UnsupportedEncodingException, IOException, ClientProtocolException {
    url = serviceUrl(url);
    HttpPost request = new HttpPost(url);
    if (headers != null) {
      for (Header header : headers) {
        request.addHeader(header);
      }
    }
    setCredentials(request);
    request.setEntity(entity);
    LOG.info("POST request : {}", url);
    return client.execute(request);
  }

  protected HttpResponse executePut(String url, HttpEntity entity, Header... headers) throws AuthenticationException,
      UnsupportedEncodingException,
      IOException, ClientProtocolException {
    url = serviceUrl(url);
    HttpPut request = new HttpPut(url);
    for (Header header : headers) {
      request.addHeader(header);
    }
    setCredentials(request);
    request.setEntity(entity);
    LOG.info("PUT request : {}", url);
    return client.execute(request);
  }

  protected void setCredentials(HttpRequest request) throws AuthenticationException {
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(NEXUS_USERNAME, NEXUS_PASSWORD);
    request.addHeader(new BasicScheme().authenticate(creds, request));
  }

  protected String executeGet(String url) throws AuthenticationException, IOException {
    HttpResponse response = executeGetWithResponse(url);
    assertEquals(SC_OK, statusCode(response));
    return EntityUtils.toString(response.getEntity());
  }

  protected HttpResponse executeGetWithResponse(String url) throws AuthenticationException, IOException {
    url = serviceUrl(url);

    HttpGet request = new HttpGet(url);
    setCredentials(request);
    LOG.info("GET request : {}", url);
    return client.execute(request);
  }

  private String serviceUrl(String url) {
    if (!url.startsWith("http")) {
      return SERVICE_BASE_URL + url;
    }
    return url;
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
    final InputStream contentStream = response.getEntity().getContent();
    try {
      return IOUtils.toString(contentStream);
    } finally {
      contentStream.close();
    }
  }

  protected int deployRpm(File rpmFile, String artifactId, String groupId, String version, String repositoryId) throws HttpException,
      IOException {
    final NexusRestClient nexusRestClient = new NexusRestClient(testContext());
    final Gav gav = new Gav(groupId, artifactId, version, null, "rpm", null, null, null, false, null, false, null);
    return new DeployUtils(nexusRestClient).deployUsingGavWithRest(repositoryId, gav, rpmFile);
  }

  protected int deployRpm(String artifactId, String groupId, String version, String repositoryId) throws NoSuchAlgorithmException,
      IOException {
    return deployRpm(createDummyRpm(artifactId, version), artifactId, groupId, version, repositoryId);
  }

  protected void givenTestRepository(String repositoryId) throws Exception {
    HttpResponse response = executeDeleteWithResponse("/repositories/" + repositoryId);
    consume(response.getEntity());
    response = executePost("/repositories", createRepositoryXml(repositoryId));
    assertEquals(content(response), SC_CREATED, statusCode(response));
  }

  private StringEntity createRepositoryXml(String repositoryId) throws UnsupportedEncodingException {
    StringEntity entity = new StringEntity(format(REPO_XML, SERVICE_BASE_URL, repositoryId, repositoryId, repositoryId));
    entity.setContentType("application/xml");
    return entity;
  }

  protected String gzipResponseContent(HttpResponse response) throws IOException {
    final InputStream content = response.getEntity().getContent();
    try {
      return IOUtils.toString(new GZIPInputStream(content));
    } finally {
      content.close();
    }
  }

  protected void givenGroupRepository(String repoId, String providerId, Repository... memberRepos) throws AuthenticationException,
      UnsupportedEncodingException, IOException, ClientProtocolException {
    HttpResponse response = executeDeleteWithResponse(format("/repo_groups/%s", repoId));
    consume(response.getEntity());

    final StringEntity content = new StringEntity(format("{data:{id: '%s', name: '%s', provider: '%s', exposed: true, repositories: [%s]}}", repoId, repoId, providerId,
        render(memberRepos)));
    response = executePost(
        "/repo_groups",
        content, new BasicHeader("Content-Type", "application/json"));
    consume(response.getEntity());
    assertThat(response.getStatusLine().getStatusCode(), is(201));
  }

  protected void wait(int timeout, TimeUnit unit) throws InterruptedException {
    Thread.sleep(unit.toMillis(timeout));
  }

  protected TestContext testContext() {
    final TestContext testContext = new TestContext();
    testContext.setNexusUrl(NEXUS_BASE_URL + "/");
    testContext.setUsername(NEXUS_USERNAME);
    testContext.setPassword(NEXUS_PASSWORD);
    testContext.setSecureTest(true);
    return testContext;
  }

  private String render(Repository[] repos) {
    if (repos == null || repos.length == 0) {
      return "";
    }

    final StringBuffer buf = new StringBuffer();
    for (Repository repo : repos) {
      if (buf.length() > 0) {
        buf.append(",");
      }
      buf.append(render(repo));
    }

    return buf.toString();
  }

  private String render(Repository repo) {
    return format("{id : '%s', name: '%s'}", repo.getId(), repo.getName());
  }

  protected void givenUploadedRpmToStaging(String groupId, String artifactId, String version) throws NoSuchAlgorithmException, IOException {
    File rpmFile = createDummyRpm(artifactId, version);
    executeMaven("deploy:deploy-file", "-Dfile=" + rpmFile.getAbsolutePath(),
      "-Durl=" + SERVICE_BASE_URL + "/staging/deploy/maven2", "-DgroupId=" + groupId,
      "-DartifactId=" + artifactId,
      "-Dversion=" + version, "-Dpackaging=rpm",
      "-X",
      "-DrepositoryId=local-nexus", "-s",
      "../src/test/resources/maven/settings.xml");
  }

  protected void executeMaven(String... params) {
    final MavenCli maven = new MavenCli();
    int exitCode = maven.doMain(params, "target", null, null);

    if (exitCode != 0) {
      throw new RuntimeException("Maven ended with exit code " + exitCode);
    }
  }
}
