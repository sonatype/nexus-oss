package org.sonatype.nexus.plugins.yum.plugin;

import static java.lang.String.format;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.util.EntityUtils.consume;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;

public class RepositoryTestService {

  private final NexusClient client;
  private final HttpClient httpClient;

  public RepositoryTestService(NexusClient client) {
    this.client = client;
    this.httpClient = new DefaultHttpClient();
  }

  public boolean deleteGroupRepo(String repoId) throws IOException, AuthenticationException {
    HttpResponse response = send(new HttpDelete(serviceUrl("/repo_groups/" + repoId)));
    consume(response.getEntity());
    return response.getStatusLine().getStatusCode() == SC_NO_CONTENT;
  }

  private HttpResponse send(HttpUriRequest request) throws IOException, AuthenticationException {
    request.addHeader(new BasicScheme().authenticate(credentials(), request));
    return httpClient.execute(request);
  }

  private Credentials credentials() {
    UsernamePasswordAuthenticationInfo authenticationInfo = (UsernamePasswordAuthenticationInfo) client.getConnectionInfo()
        .getAuthenticationInfo();
    return new UsernamePasswordCredentials(authenticationInfo.getUsername(), authenticationInfo.getPassword());
  }

  private String serviceUrl(String pathToService) {
    return client.getConnectionInfo().getBaseUrl() + "service/local" + pathToService;
  }

  public boolean createGroupRepo(String repoId, String providerId) throws AuthenticationException, IOException {
    String content = format("{data:{id: '%s', name: '%s', provider: '%s', exposed: true, repositories: []}}", repoId, repoId, providerId);
    HttpResponse response = send(postJson(serviceUrl("/repo_groups"), content));
    consume(response.getEntity());
    return response.getStatusLine().getStatusCode() == SC_CREATED;
  }

  private HttpPost postJson(String serviceUrl, String content) throws IOException {
    HttpPost request = new HttpPost(serviceUrl);
    request.setEntity(new StringEntity(content));
    request.addHeader(new BasicHeader("Content-Type", "application/json"));
    return request;
  }

}
