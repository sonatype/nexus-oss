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
package org.sonatype.nexus.testsuite.security;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.text.Strings2;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.storage.WritePolicy;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.SetCookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static com.google.common.net.HttpHeaders.SET_COOKIE;
import static com.google.common.net.HttpHeaders.USER_AGENT;
import static com.google.common.net.HttpHeaders.X_REQUESTED_WITH;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@ExamReactorStrategy(PerClass.class)
public class SimpleSessionCookieIT
    extends NexusHttpsITSupport
{
  private static final String PROTECTED_PATH = "internal/metrics";

  private static final String SESSION_PATH = "service/rapture/session";

  private static final String TYPICAL_BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.99 Safari/537.36";

  private static final String TEST_REPOSITORY_NAME = "test-repository";

  @Inject
  private RepositoryManager repositoryManager;

  private Repository testRepository;

  @Before
  public void prepare() throws Exception {
    if (repositoryManager.get(TEST_REPOSITORY_NAME) == null) {
      final Configuration testRepositoryConfig = new Configuration();
      testRepositoryConfig.setRecipeName("raw-hosted"); // using name here to not complicate importing of internal class
      testRepositoryConfig.setRepositoryName(TEST_REPOSITORY_NAME);
      testRepositoryConfig.setOnline(true);

      NestedAttributesMap storage = testRepositoryConfig.attributes("storage");
      storage.set("blobStoreName", BlobStoreManager.DEFAULT_BLOBSTORE_NAME);
      storage.set("writePolicy", WritePolicy.ALLOW.toString());

      testRepository = repositoryManager.create(testRepositoryConfig);
    }
  }

  @Test
  public void authenticatedContentCRUDActionsShouldNotCreateSession() throws Exception {
    final String target = resolveUrl(nexusUrl, "repository/" + testRepository.getName() + "/foo/bar/1/bar-1.txt")
        .toExternalForm();

    final HttpPut put = new HttpPut(target);
    put.setEntity(new StringEntity("text content"));
    try (CloseableHttpClient client = clientBuilder().setDefaultCredentialsProvider(credentialsProvider()).build()) {
      try (CloseableHttpResponse response = client.execute(put, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(201));
        assertResponseHasNoSessionCookies(response);
      }
    }

    final HttpHead head = new HttpHead(target);
    try (CloseableHttpClient client = clientBuilder().setDefaultCredentialsProvider(credentialsProvider()).build()) {
      try (CloseableHttpResponse response = client.execute(head, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertResponseHasNoSessionCookies(response);
      }
    }

    final HttpGet get = new HttpGet(target);
    try (CloseableHttpClient client = clientBuilder().setDefaultCredentialsProvider(credentialsProvider()).build()) {
      try (CloseableHttpResponse response = client.execute(get, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertResponseHasNoSessionCookies(response);
      }
    }

    final HttpDelete delete = new HttpDelete(target);
    try (CloseableHttpClient client = clientBuilder().setDefaultCredentialsProvider(credentialsProvider()).build()) {
      try (CloseableHttpResponse response = client.execute(delete, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(204));
        assertResponseHasNoSessionCookies(response);
      }
    }
  }

  @Test
  public void sessionCookieSpecUsingHttp() throws Exception {
    exerciseCookieSpec(nexusUrl);
  }

  @Test
  public void sessionCookieSpecUsingHttps() throws Exception {
    exerciseCookieSpec(nexusSecureUrl);
  }

  private static void assertResponseHasNoSessionCookies(final HttpResponse response) {
    Header[] headers = response.getHeaders(SET_COOKIE);
    for (Header header : headers) {
      assertThat(String.format("not expecting any %s session cookie headers but got %s", SET_COOKIE, asList(headers)),
          header.getValue(), not(startsWith(DEFAULT_SESSION_COOKIE_NAME)));
    }
  }

  private static HttpRequestBase withCommonBrowserHeaders(HttpRequestBase req) {
    req.setHeader(USER_AGENT, TYPICAL_BROWSER_USER_AGENT);
    req.setHeader("X-Nexus-UI", "true");
    req.setHeader(X_REQUESTED_WITH, "XMLHttpRequest");
    return req;
  }

  /**
   * Validate Nexus Cookies during Sign-in and Sign-out
   */
  private void exerciseCookieSpec(final URL baseUrl) throws Exception {
    // handle cookies like a browser to aid validation
    final CookieSpec spec = new DefaultCookieSpecProvider().create(null);
    final CookieOrigin cookieOrigin = cookieOrigin(baseUrl);
    final CookieStore cookieStore = new BasicCookieStore();
    final CredentialsProvider credProvider = credentialsProvider();
    SetCookie loginCookie;

    try (CloseableHttpClient client = clientBuilder().setDefaultCookieStore(cookieStore)
        .setDefaultCredentialsProvider(credProvider).build()) {

      // 1. login with credentials and get session cookie
      // Set-Cookie: NXSESSIONID=98a766bc-bc33-4b3c-9d9f-d3bb85b0cf00; Path=/; Secure; HttpOnly

      HttpPost loginRequest = new HttpPost(resolveUrl(baseUrl, SESSION_PATH).toURI());
      List<NameValuePair> params = new ArrayList<>();
      params.add(new BasicNameValuePair("username", Strings2.encodeBase64(credentials().getUserPrincipal().getName())));
      params.add(new BasicNameValuePair("password", Strings2.encodeBase64(credentials().getPassword())));
      loginRequest.setEntity(new UrlEncodedFormEntity(params));
      withCommonBrowserHeaders(loginRequest);

      try (CloseableHttpResponse response = client.execute(loginRequest, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(204));
        assertThat("login cookie should have been stored in the cookie store", cookieStore.getCookies(), hasSize(1));
        assertThat("expected session cookie in cookie store", getSessionCookie(cookieStore), notNullValue());

        Header[] setCookieHeaders = response.getHeaders(SET_COOKIE);
        Header sessionCookieHeader = getSessionCookieHeader(setCookieHeaders);

        List<Cookie> sessionCookies = spec.parse(sessionCookieHeader, cookieOrigin);
        loginCookie = (SetCookie) sessionCookies.get(0);
        String headerText = sessionCookieHeader.toString();

        assertCommonSessionCookieAttributes(baseUrl, loginCookie, headerText);
        assertThat(String.format("expecting one cookie parsed from session %s header", SET_COOKIE), sessionCookies,
            hasSize(1));

        assertThat(String.format("expecting 2 %s headers for login, one session cookie, one remember me, but got %s",
            SET_COOKIE, setCookieHeaders), setCookieHeaders, arrayWithSize(2));

        assertThat("login cookie should NOT look like deleteMe cookie", loginCookie.getValue(),
            not(containsString("deleteMe")));
        assertThat("login cookie should not have an expiry date - the UA deletes the session cookie when "
                + "replaced by a new one by same name from the server OR when the UA decides",
            loginCookie.isPersistent(),
            is(false));

        assertThat("login session cookie with valid session id should always be marked HttpOnly", headerText,
            containsString("; HttpOnly"));
      }

      HttpClientContext logoutContext = HttpClientContext.create();
      logoutContext.setCookieStore(cookieStore);

      HttpDelete logoutRequest = new HttpDelete(resolveUrl(baseUrl, SESSION_PATH).toURI());
      withCommonBrowserHeaders(logoutRequest);

      // 2. Logout, sending valid session cookie, no credentials
      // Set-Cookie: NXSESSIONID=deleteMe; Path=/; Max-Age=0; Expires=Sun, 28-Dec-2014 15:59:11 GMT
      try (CloseableHttpResponse response = client.execute(logoutRequest, logoutContext)) {
        assertThat(response.getStatusLine().getStatusCode(), is(204));

        // can't use client CookieStore to examine logout cookie, because the Expires header will prevent it from being
        // added but we can implicitly confirm it expired the existing cookie according to our client
        assertThat("logout cookie should have emptied the cookie store due to expiry date", cookieStore.getCookies(),
            hasSize(0));

        Header[] setCookieHeaders = response.getHeaders(SET_COOKIE);
        Header sessionCookieHeader = getSessionCookieHeader(setCookieHeaders);
        List<Cookie> sessionCookies = spec.parse(sessionCookieHeader, cookieOrigin);
        SetCookie logoutCookie = (SetCookie) sessionCookies.get(0);
        final String headerText = sessionCookieHeader.toString();

        assertCommonSessionCookieAttributes(baseUrl, logoutCookie, headerText);
        assertThat("expecting one cookie in same Set-Cookie header", sessionCookies, hasSize(1));
        assertThat(
            String.format(
                "expecting 2 %s headers for logout, one session cookie delete cookie, one remember me delete cookie, but got %s",
                SET_COOKIE, setCookieHeaders), setCookieHeaders, arrayWithSize(2));
        assertThat("logout session cookie value should be dummy value", logoutCookie.getValue(), equalTo("deleteMe"));
        assertThat("logout session cookie should be expired to tell browser to delete it",
            logoutCookie.isExpired(new Date()), is(true));
        assertThat(
            "technically the presence of an expiry date means the cookie is persistent, but expiry will override",
            logoutCookie.isPersistent(), is(true));
        assertThat("logout cookie does not have a real session id value, therefore it does not need to be HttpOnly",
            headerText, not(containsString("; HttpOnly")));
      }

      // 3. Access a protected resource again using our original login cookie, no credentials, to verify session is dead
      HttpGet loginFailedGet = new HttpGet(resolveUrl(baseUrl, PROTECTED_PATH).toURI());
      cookieStore.addCookie(loginCookie);

      try (CloseableHttpResponse response = client.execute(loginFailedGet, HttpClientContext.create())) {
        assertThat("expected dead login session cookie to not authenticate", response.getStatusLine().getStatusCode(),
            is(401));
        Header[] setCookieHeaders = response.getHeaders(SET_COOKIE);
        assertThat("expecting no session cookie since login was unsuccessful",
            getSessionCookieHeader(setCookieHeaders), nullValue());
        assertThat("expecting no cookies since login was unsuccessful", setCookieHeaders, arrayWithSize(0));
      }
    }
  }

  /**
   * Validate standard session cookie properties
   */
  private void assertCommonSessionCookieAttributes(final URL baseUrl,
                                                   final SetCookie serverCookie,
                                                   final String headerText)
  {
    assertThat(serverCookie, notNullValue());
    assertThat("cookie value must be present", serverCookie.getValue(), notNullValue());
    assertThat("cookie name mismatch", serverCookie.getName(), equalTo(DEFAULT_SESSION_COOKIE_NAME));
    assertThat("not expecting to get ports since they are generally ignored", serverCookie.getPorts(), nullValue());
    assertThat("session cookie does not currently set the domain leaving it to the Browser to do the right thing",
        headerText, not(containsString("; Domain=")));
    assertThat("browser should interpret domain as same as origin", serverCookie.getDomain(),
        equalTo(baseUrl.getHost()));
    assertThat("cookie path should match Nexus context path", serverCookie.getPath(), equalTo(cookiePath(baseUrl)));
    if (baseUrl.getProtocol().equalsIgnoreCase("https")) {
      assertThat("session cookie should be marked Secure when cookies are served by https URLs", headerText,
          containsString("; Secure"));
    }
    else {
      assertThat("session cookie should not be marked Secure when cookies are served by http URLs", headerText,
          not(containsString("; Secure")));
    }
  }
}
