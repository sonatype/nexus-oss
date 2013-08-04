/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.security.nexus4383;

import java.net.URI;
import java.util.Collection;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests to make sure the session is removed when the logout resource is called.
 */
public class Nexus4383LogoutResourceIT
    extends AbstractNexusIntegrationTest
{

  @BeforeClass
  public static void setSecureTest() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Override
  protected void runOnce()
      throws Exception
  {
    // disable anonymous access
    GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();
    settings.setSecurityAnonymousAccessEnabled(false);
    SettingsMessageUtil.save(settings);

  }


  /**
   * 1.) Make a get request to set a cookie </BR>
   * 2.) verify cookie works (do not send basic auth) </BR>
   * 3.) do logout  </BR>
   * 4.) repeat step 2 and expect failure.
   */
  @Test
  public void testLogout()
      throws Exception
  {
    TestContext context = TestContainer.getInstance().getTestContext();
    String username = context.getAdminUsername();
    String password = context.getPassword();
    String url = this.getBaseNexusUrl() + RequestFacade.SERVICE_LOCAL + "status";
    String logoutUrl = this.getBaseNexusUrl() + RequestFacade.SERVICE_LOCAL + "authentication/logout";

    Header userAgentHeader = new BasicHeader("User-Agent", "Something Stateful");

    // default useragent is: Jakarta Commons-HttpClient/3.1[\r][\n]
    DefaultHttpClient httpClient = new DefaultHttpClient();
    URI nexusBaseURI = new URI(url);
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

    HttpGet getMethod = new HttpGet(url);
    getMethod.addHeader(userAgentHeader);
    try {
      Assert.assertEquals(httpClient.execute(getMethod, localcontext).getStatusLine().getStatusCode(), 200);
    }
    finally {
      getMethod.reset();
    }

    Cookie sessionCookie = this.getSessionCookie(httpClient.getCookieStore().getCookies());
    Assert.assertNotNull("Session Cookie not set", sessionCookie);

    httpClient.getCookieStore().clear(); // remove cookies
    httpClient.getCredentialsProvider().clear(); // remove auth

    // now with just the cookie
    httpClient.getCookieStore().addCookie(sessionCookie);
    getMethod = new HttpGet(url);
    try {
      Assert.assertEquals(httpClient.execute(getMethod).getStatusLine().getStatusCode(), 200);
    }
    finally {
      getMethod.reset();
    }

    // do logout
    HttpGet logoutGetMethod = new HttpGet(logoutUrl);
    try {
      final HttpResponse response = httpClient.execute(logoutGetMethod);
      Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
      Assert.assertEquals("OK", EntityUtils.toString(response.getEntity()));
    }
    finally {
      logoutGetMethod.reset();
    }

    // set cookie again
    httpClient.getCookieStore().clear(); // remove cookies
    httpClient.getCredentialsProvider().clear(); // remove auth

    httpClient.getCookieStore().addCookie(sessionCookie);
    HttpGet failedGetMethod = new HttpGet(url);
    try {
      final HttpResponse response = httpClient.execute(failedGetMethod);
      Assert.assertEquals(response.getStatusLine().getStatusCode(), 401);
    }
    finally {
      failedGetMethod.reset();
    }
  }

  private Cookie getSessionCookie(Collection<Cookie> cookies) {
    for (Cookie cookie : cookies) {
      if ("JSESSIONID".equals(cookie.getName())) {
        return cookie;
      }
    }

    return null;

  }

}
