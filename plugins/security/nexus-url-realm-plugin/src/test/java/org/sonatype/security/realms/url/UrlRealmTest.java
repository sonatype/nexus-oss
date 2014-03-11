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

package org.sonatype.security.realms.url;

import com.sonatype.security.realms.url.config.model.Configuration;

import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.security.realms.url.config.UrlRealmConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.BasicAuth;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

public class UrlRealmTest
    extends TestSupport
{
  protected static final String username = "popeye";

  protected static final String password = "spinach";

  protected static final String DEFAULT_ROLE = "default-url-role";

  @Mock
  protected ApplicationConfiguration applicationConfiguration;

  @Mock
  protected UrlRealmConfiguration urlRealmConfiguration;

  @Mock
  protected Hc4Provider hc4Provider;

  protected Server server;

  protected CloseableHttpClient httpClient;

  protected UrlRealm urlRealm;

  @Before
  public void prepare() throws Exception {
    server = Server.server().port(0).serve("/*").withBehaviours(new BasicAuth(username, password),
        Behaviours.content("not important"));
    server.start();

    // build client
    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
    credsProvider.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(username, password));
    httpClient = HttpClients.custom()
        .setDefaultCredentialsProvider(credsProvider)
        .build();

    // prepare URLRealm
    when(applicationConfiguration.getGlobalRemoteStorageContext()).thenReturn(new DefaultRemoteStorageContext(null));

    final Configuration model = new Configuration();
    model.setVersion(Configuration.MODEL_VERSION);
    model.setBaseUrl(server.getUrl().toString() + "/foo");
    model.setDefaultRole(DEFAULT_ROLE);
    when(urlRealmConfiguration.getConfiguration()).thenReturn(model);

    when(hc4Provider.createHttpClient(Mockito.any(RemoteStorageContext.class))).thenReturn(httpClient);
    urlRealm = new UrlRealm(applicationConfiguration, urlRealmConfiguration, hc4Provider);
  }

  @After
  public void cleanUp() throws Exception {
    try {
      if (httpClient != null) {
        httpClient.close();
      }
    }
    finally {
      server.stop();
    }
  }

  // ==

  @Test
  public void testAuthc()
      throws Exception
  {
    final AuthenticationInfo info = urlRealm.getAuthenticationInfo(new UsernamePasswordToken(username, password));
    assertThat(info, notNullValue());
  }

  @Test(expected = UnknownAccountException.class)
  public void testAuthcWrongCreds()
      throws Exception
  {
    urlRealm.getAuthenticationInfo(new UsernamePasswordToken(username, "cake"));
  }

  @Test(expected = UnknownAccountException.class)
  public void testAuthcJunkCreds()
      throws Exception
  {
    urlRealm.getAuthenticationInfo(new UsernamePasswordToken("fakeuser", "hack-me-in"));
  }

  @Test
  public void testAuthz()
      throws Exception
  {
    urlRealm.checkRole(new SimplePrincipalCollection(username, urlRealm.getName()), DEFAULT_ROLE);
  }

  @Test(expected = UnauthorizedException.class)
  public void testAuthzWrongRole()
      throws Exception
  {
    urlRealm.checkRole(new SimplePrincipalCollection(username, urlRealm.getName()), "Not-Existing-Role");
  }
}
