/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
import java.util.Date;
import java.util.List;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.client.core.subsystem.ServerConfiguration;
import org.sonatype.nexus.client.core.subsystem.config.Security;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
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
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
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

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class SimpleSessionCookieIT
    extends NexusHttpsITSupport
{

  private static final String LEGACY_LOGIN_PATH = "service/local/authentication/login";

  private static final String LEGACY_LOGOUT_PATH = "service/local/authentication/logout";

  private static final String EXTDIRECT_PATH = "service/extdirect";

  private static final String TYPICAL_BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.99 Safari/537.36";


  public SimpleSessionCookieIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    // help verify that a custom context path sets the cookie path value correctly
    return super.configureNexus(configuration).setContextPath("/customcontextpath");
  }

  @Before
  public void disableAnonymousSoThatAllRequestsRequireAuthentication() throws Exception {
    Security security = client().getSubsystem(ServerConfiguration.class).security();
    security.settings().withAnonymousAccessEnabled(false);
    security.save();
  }

  @Test
  public void authenticatedContentCRUDActionsShouldNotCreateSession() throws Exception {
    final String target = nexus().getUrl() + "content/repositories/releases/test.txt";

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

  private void assertResponseHasNoSessionCookies(final HttpResponse response) {
    Header[] headers = response.getHeaders(SET_COOKIE);
    for (Header header : headers) {
      assertThat(String.format("not expecting any %s session cookie headers but got %s", SET_COOKIE, asList(headers)),
          header.getValue(), not(startsWith(DEFAULT_SESSION_COOKIE_NAME)));
    }
  }

  @Test
  public void sessionCookieSpecUsingHttpWithLegacyResources() throws Exception {
    exerciseCookieSpec(nexus().getUrl(), true);
  }

  @Test
  public void sessionCookieSpecUsingHttpsWithLegacyResources() throws Exception {
    exerciseCookieSpec(nexus().getSecureUrl(), true);
  }

  @Test
  public void sessionCookieSpecUsingHttpWithModernResources() throws Exception {
    exerciseCookieSpec(nexus().getUrl(), false);
  }

  @Test
  public void sessionCookieSpecUsingHttpsWithModernResources() throws Exception {
    exerciseCookieSpec(nexus().getSecureUrl(), false);
  }

  private HttpRequestBase withCommonBrowserHeaders(HttpRequestBase req) {
    req.setHeader(USER_AGENT, TYPICAL_BROWSER_USER_AGENT);
    req.setHeader("X-Nexus-UI", "true");
    req.setHeader(X_REQUESTED_WITH, "XMLHttpRequest");
    return req;
  }

  /**
   * Validate Nexus Cookies during Sign-in and Sign-out
   *
   * @param nexusUrl           the base Nexus URL to validate against
   * @param useLegacyResources true to use legacy resources where available, false to use modern resources
   */
  private void exerciseCookieSpec(final URL nexusUrl, boolean useLegacyResources) throws Exception {

    // handle cookies like a browser to aid validation
    final CookieSpec spec = new BrowserCompatSpecFactory().create(null);
    final CookieOrigin cookieOrigin = cookieOrigin(nexusUrl);
    final CookieStore cookieStore = new BasicCookieStore();
    final CredentialsProvider credProvider = credentialsProvider();
    SetCookie loginCookie;

    try (CloseableHttpClient client = clientBuilder().setDefaultCookieStore(cookieStore).
        setDefaultCredentialsProvider(credProvider).build()) {

      // 1. login with credentials and get session cookie
      // Set-Cookie: NXSESSIONID=98a766bc-bc33-4b3c-9d9f-d3bb85b0cf00; Path=/; Secure; HttpOnly
      HttpRequestBase loginRequest = new HttpGet(nexusUrl.toExternalForm() + LEGACY_LOGIN_PATH);
      withCommonBrowserHeaders(loginRequest);
      try (CloseableHttpResponse response = client.execute(loginRequest, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat("login cookie should have been stored in the cookie store", cookieStore.getCookies(),
            hasSize(1));
        assertThat("expected session cookie in cookie store", getSessionCookie(cookieStore), notNullValue());

        Header[] setCookieHeaders = response.getHeaders(SET_COOKIE);
        Header sessionCookieHeader = getSessionCookieHeader(setCookieHeaders);

        List<Cookie> sessionCookies = spec.parse(sessionCookieHeader, cookieOrigin);
        loginCookie = (SetCookie) sessionCookies.get(0);
        String headerText = sessionCookieHeader.toString();

        assertCommonSessionCookieAttributes(nexusUrl, loginCookie, headerText);
        assertThat(String.format("expecting one cookie parsed from session %s header", SET_COOKIE), sessionCookies,
            hasSize(1));

        assertThat(String
            .format("expecting 2 %s headers for login, one session cookie, one remember me, but got %s", SET_COOKIE,
                setCookieHeaders), setCookieHeaders, arrayWithSize(2));

        assertThat("login cookie should NOT look like deleteMe cookie", loginCookie.getValue(), not(containsString(
            "deleteMe")));
        assertThat("login cookie should not have an expiry date - the UA deletes the session cookie when " +
                "replaced by a new one by same name from the server OR when the UA decides",
            loginCookie.isPersistent(), is(false));

        assertThat("login session cookie with valid session id should always be marked HttpOnly",
            headerText, containsString("; HttpOnly"));
      }

      HttpClientContext logoutContext = HttpClientContext.create();
      logoutContext.setCookieStore(cookieStore);

      HttpRequestBase logoutRequest;
      if (useLegacyResources) {
        logoutRequest = new HttpGet(nexusUrl.toExternalForm() + LEGACY_LOGOUT_PATH);
      }
      else {
        HttpPost logout = new HttpPost(nexusUrl.toExternalForm() + EXTDIRECT_PATH);
        logout.setEntity(new StringEntity(
            new ExtDirectTransactionBuilder().withAction("rapture_Security").withMethod("signOut").toJson()));
        logout.setHeader(CONTENT_TYPE, "application/json");
        logoutRequest = logout;
      }
      withCommonBrowserHeaders(logoutRequest);

      // 2. Logout, sending valid session cookie, no credentials
      // Set-Cookie: NXSESSIONID=deleteMe; Path=/; Max-Age=0; Expires=Sun, 28-Dec-2014 15:59:11 GMT
      try (CloseableHttpResponse response = client.execute(logoutRequest, logoutContext)) {
        assertThat(response.getStatusLine().getStatusCode(), is(200));

        // can't use client CookieStore to examine logout cookie, because the Expires header will prevent it from being
        // added but we can implicitly confirm it expired the existing cookie according to our client
        assertThat("logout cookie should have emptied the cookie store due to expiry date", cookieStore.getCookies(),
            hasSize(0));

        Header[] setCookieHeaders = response.getHeaders(SET_COOKIE);
        Header sessionCookieHeader = getSessionCookieHeader(setCookieHeaders);
        List<Cookie> sessionCookies = spec.parse(sessionCookieHeader, cookieOrigin);
        SetCookie logoutCookie = (SetCookie) sessionCookies.get(0);
        final String headerText = sessionCookieHeader.toString();

        assertCommonSessionCookieAttributes(nexusUrl, logoutCookie, headerText);
        assertThat("expecting one cookie in same Set-Cookie header", sessionCookies, hasSize(1));
        assertThat(String.format(
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
      HttpGet loginFailedGet = new HttpGet(nexusUrl.toExternalForm() + LEGACY_LOGIN_PATH);
      cookieStore.addCookie(loginCookie);
      try (CloseableHttpResponse response = client.execute(loginFailedGet, HttpClientContext.create())) {
        assertThat("expected dead login session cookie to not authenticate", response.getStatusLine().getStatusCode(),
            is(401));
        Header[] setCookieHeaders = response.getHeaders(SET_COOKIE);
        assertThat("expecting no session cookie since login was unsuccessful", getSessionCookieHeader(setCookieHeaders),
            nullValue());
        assertThat("expecting no cookies since login was unsuccessful", setCookieHeaders, arrayWithSize(0));
      }
    }
  }

  /**
   * Validate standard session cookie properties
   */
  protected void assertCommonSessionCookieAttributes(final URL nexusUrl, final SetCookie serverCookie,
                                                     final String headerText)
  {
    assertThat(serverCookie, notNullValue());
    assertThat("cookie value must be present", serverCookie.getValue(), notNullValue());
    assertThat("cookie name mismatch", serverCookie.getName(), equalTo(DEFAULT_SESSION_COOKIE_NAME));
    assertThat("not expecting to get ports since they are generally ignored", serverCookie.getPorts(), nullValue());
    assertThat("session cookie does not currently set the domain leaving it to the Browser to do the right thing",
        headerText, not(containsString("; Domain=")));
    assertThat("browser should interpret domain as same as origin", serverCookie.getDomain(),
        equalTo(nexusUrl.getHost()));
    assertThat("cookie path should match Nexus context path", serverCookie.getPath(),
        equalTo(expectedCookiePath(nexusUrl)));
    if (nexusUrl.getProtocol().equalsIgnoreCase("https")) {
      assertThat("session cookie should be marked Secure when cookies are served by https URLs",
          headerText, containsString("; Secure"));
    }
    else {
      assertThat("session cookie should not be marked Secure when cookies are served by http URLs",
          headerText, not(containsString("; Secure")));
    }
  }
}
