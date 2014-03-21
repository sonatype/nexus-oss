/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.security.nexus4257;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class Nexus4257CookieVerificationIT
    extends AbstractNexusIntegrationTest
{
  /**
   * Copied from deprecated org.sonatype.nexus.security.StatelessAndStatefulWebSessionManager to
   * cut the direct plugin class dependency from ITs.
   */
  public static final String NO_SESSION_HEADER = "X-Nexus-Session";

  @BeforeClass
  public static void setSecureTest() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Test
  public void testCookieForStateFullClient()
      throws Exception
  {
    setAnonymousAccess(false);

    TestContext context = TestContainer.getInstance().getTestContext();
    String username = context.getAdminUsername();
    String password = context.getPassword();
    String url = this.getBaseNexusUrl() + "content/";
    URI nexusBaseURI = new URI(url);

    DefaultHttpClient httpClient = new DefaultHttpClient();
    httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "SomeUAThatWillMakeMeLookStateful/1.0");
    final BasicHttpContext localcontext = new BasicHttpContext();
    final HttpHost targetHost =
        new HttpHost(nexusBaseURI.getHost(), nexusBaseURI.getPort(), nexusBaseURI.getScheme());
    httpClient.getCredentialsProvider().setCredentials(
        new AuthScope(targetHost.getHostName(), targetHost.getPort()),
        new UsernamePasswordCredentials(username, password));
    AuthCache authCache = new BasicAuthCache();
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(targetHost, basicAuth);
    localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

    // stateful clients must login first, since other rest urls create no sessions
    String loginUrl = this.getBaseNexusUrl() + "service/local/authentication/login";
    assertThat(executeAndRelease(httpClient, new HttpGet(loginUrl), localcontext), equalTo(200));

    // after login check content but make sure only cookie is used
    httpClient.getCredentialsProvider().clear();
    HttpGet getMethod = new HttpGet(url);
    assertThat(executeAndRelease(httpClient, getMethod, null), equalTo(200));
    Cookie sessionCookie = this.getSessionCookie(httpClient.getCookieStore().getCookies());
    assertThat("Session Cookie not set", sessionCookie, notNullValue());
    httpClient.getCookieStore().clear(); // remove cookies

    // do not set the cookie, expect failure
    HttpGet failedGetMethod = new HttpGet(url);
    assertThat(executeAndRelease(httpClient, failedGetMethod, null), equalTo(401));

    // set the cookie expect a 200, If a cookie is set, and cannot be found on the server, the response will fail
    // with a 401
    httpClient.getCookieStore().addCookie(sessionCookie);
    getMethod = new HttpGet(url);
    assertThat(executeAndRelease(httpClient, getMethod, null), equalTo(200));
  }

  /**
   * Tests that session cookies are not set for the list of known stateless clients.
   */
  @Test
  public void testCookieForStateLessClient()
      throws Exception
  {
    setAnonymousAccess(false);

    String[] statelessUserAgents = {
        "Java",
        "Apache-Maven",
        "Apache Ivy",
        "curl",
        "Wget",
        "Nexus",
        "Artifactory",
        "Apache Archiva",
        "M2Eclipse",
        "Aether"
    };

    for (String userAgent : statelessUserAgents) {
      testCookieNotSetForKnownStateLessClients(userAgent);
    }
  }

  /**
   * Makes a request after setting the user agent and verifies that the session cookie is NOT set.
   */
  private void testCookieNotSetForKnownStateLessClients(final String userAgent)
      throws Exception
  {
    TestContext context = TestContainer.getInstance().getTestContext();
    String username = context.getAdminUsername();
    String password = context.getPassword();
    String url = this.getBaseNexusUrl() + "content/";
    URI uri = new URI(url);

    Header header = new BasicHeader("User-Agent", userAgent + "/1.6"); // user agent plus some version

    DefaultHttpClient httpClient = new DefaultHttpClient();

    final BasicHttpContext localcontext = new BasicHttpContext();
    final HttpHost targetHost =
        new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
    httpClient.getCredentialsProvider().setCredentials(
        new AuthScope(targetHost.getHostName(), targetHost.getPort()),
        new UsernamePasswordCredentials(username, password));
    AuthCache authCache = new BasicAuthCache();
    BasicScheme basicAuth = new BasicScheme();
    authCache.put(targetHost, basicAuth);
    localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

    HttpGet getMethod = new HttpGet(url);
    getMethod.addHeader(header);
    assertThat(executeAndRelease(httpClient, getMethod, localcontext), equalTo(200));

    Cookie sessionCookie = this.getSessionCookie(httpClient.getCookieStore().getCookies());
    assertThat("Session Cookie should not be set for user agent: " + userAgent, sessionCookie, nullValue());
  }

  /**
   * Tests that an anonymous user with a stateless client does NOT receive a session cookie.
   */
  @Test
  public void testCookieForStateFullClientForAnonUser()
      throws Exception
  {
    setAnonymousAccess(true);

    String url = this.getBaseNexusUrl() + "content/";

    DefaultHttpClient httpClient = new DefaultHttpClient();

    HttpGet getMethod = new HttpGet(url);
    assertThat(executeAndRelease(httpClient, getMethod, null), equalTo(200));

    Cookie sessionCookie = this.getSessionCookie(httpClient.getCookieStore().getCookies());
    assertThat("Session Cookie should not be set", sessionCookie, nullValue());
  }

  /**
   * Tests that an anonymous user with a stateless client does NOT receive a session cookie.
   */
  @Test
  public void testCookieForStateLessClientForAnonUser()
      throws Exception
  {
    setAnonymousAccess(true);

    String url = this.getBaseNexusUrl() + "content/";

    Header header = new BasicHeader("User-Agent", "Java/1.6");
    DefaultHttpClient httpClient = new DefaultHttpClient();

    HttpGet getMethod = new HttpGet(url);
    getMethod.addHeader(header);
    assertThat(executeAndRelease(httpClient, getMethod, null), equalTo(200));

    Cookie sessionCookie = this.getSessionCookie(httpClient.getCookieStore().getCookies());
    assertThat("Session Cookie should not be set", sessionCookie, nullValue());
  }

  /**
   * Verifies that requests with the header: X-Nexus-Session do not have session cookies set.
   */
  @Test
  public void testNoSessionHeader()
      throws Exception
  {
    setAnonymousAccess(true);

    String url = this.getBaseNexusUrl() + "content/";

    Header header = new BasicHeader(NO_SESSION_HEADER, "none");
    Header usHeader = new BasicHeader("User-Agent", "SomeStatefulClientThatKnowsAboutThisHeader/1.6");
    DefaultHttpClient httpClient = new DefaultHttpClient();

    HttpGet getMethod = new HttpGet(url);
    getMethod.addHeader(header);
    getMethod.addHeader(usHeader);
    assertThat(executeAndRelease(httpClient, getMethod, null), equalTo(200));

    Cookie sessionCookie = this.getSessionCookie(httpClient.getCookieStore().getCookies());
    assertThat("Session Cookie should not be set", sessionCookie, nullValue());
  }

  private void setAnonymousAccess(boolean enabled)
      throws Exception
  {
    GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
    settings.setSecurityAnonymousAccessEnabled(enabled);
    SettingsMessageUtil.save(settings);
  }

  private Cookie getSessionCookie(Collection<Cookie> cookies) {
    for (Cookie cookie : cookies) {
      if ("JSESSIONID".equals(cookie.getName())) {
        return cookie;
      }
    }

    return null;

  }

  private int executeAndRelease(HttpClient httpClient, HttpGet method, HttpContext context)
      throws IOException
  {
    int status = 0;
    try {
      if (context != null) {
        status = httpClient.execute(method, context).getStatusLine().getStatusCode();
      }
      else {
        status = httpClient.execute(method).getStatusLine().getStatusCode();
      }
    }
    finally {
      method.reset();
    }

    return status;
  }

}
