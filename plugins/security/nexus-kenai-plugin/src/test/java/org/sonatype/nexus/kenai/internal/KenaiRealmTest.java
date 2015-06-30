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
package org.sonatype.nexus.kenai.internal;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import org.sonatype.nexus.common.app.AbstractApplicationStatusSource;
import org.sonatype.nexus.common.app.ApplicationStatusSource;
import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.nexus.httpclient.HttpClientManager;
import org.sonatype.nexus.kenai.Kenai;
import org.sonatype.nexus.kenai.KenaiConfiguration;
import org.sonatype.nexus.security.WebSecurityModule;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.inject.Binder;
import com.google.inject.Module;
import junit.framework.Assert;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KenaiRealmTest
    extends NexusTestSupport
{
  protected final String username = "test-user";

  protected final String password = "test-user123";

  protected final static String DEFAULT_ROLE = "default-url-role";

  protected static final String AUTH_APP_NAME = "auth_app";

  @Rule
  public ServerResource server = new ServerResource(
      Server.server().serve("/api/login/*").withServlet(new KenaiMockAuthcServlet()).getServerProvider());

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);
    modules.add(new WebSecurityModule(mock(ServletContext.class)));
    modules.add(new Module()
    {
      @Override
      public void configure(final Binder binder) {
        ApplicationStatusSource statusSource = mock(AbstractApplicationStatusSource.class);
        when(statusSource.getSystemStatus()).thenReturn(new SystemStatus());
        binder.bind(ApplicationStatusSource.class).toInstance(statusSource);

        binder.bind(Kenai.class).toInstance(mockKenai());
      }
    });
  }

  protected Kenai mockKenai() {
    KenaiConfiguration kenaiConfiguration = new KenaiConfiguration();
    kenaiConfiguration.setBaseUrl(server.getServerProvider().getUrl() + AUTH_APP_NAME + "/");
    kenaiConfiguration.setDefaultRole(DEFAULT_ROLE);

    Kenai kenai = mock(Kenai.class);
    when(kenai.isEnabled()).thenReturn(true);
    try {
      when(kenai.getConfiguration()).thenReturn(kenaiConfiguration);
    }
    catch (IOException e) {
      // not way, we are mocking
    }
    return kenai;
  }

  private KenaiRealm getRealm() throws Exception {
    HttpClientManager httpClientManager = Mockito.mock(HttpClientManager.class);
    Mockito.when(httpClientManager.create()).thenReturn(new DefaultHttpClient());
    return new KenaiRealm(mockKenai(), httpClientManager);
  }

  @Test
  public void testAuthenticate() throws Exception {
    KenaiRealm kenaiRealm = this.getRealm();

    AuthenticationInfo info = kenaiRealm.getAuthenticationInfo(new UsernamePasswordToken(username, password));
    Assert.assertNotNull(info);
  }

  @Test
  public void testAuthorize() throws Exception {
    KenaiRealm kenaiRealm = this.getRealm();
    kenaiRealm.checkRole(new SimplePrincipalCollection(username, kenaiRealm.getName()), DEFAULT_ROLE);
  }

  @Test
  public void testAuthFail() throws Exception {
    KenaiRealm kenaiRealm = this.getRealm();

    try {
      kenaiRealm.getAuthenticationInfo(new UsernamePasswordToken("random", "JUNK-PASS"));
      Assert.fail("Expected: AccountException to be thrown");
    }
    catch (AccountException e) {
      // expected
    }
  }

  @Test
  public void testAuthFailAuthFail() throws Exception {
    KenaiRealm kenaiRealm = this.getRealm();

    try {
      Assert.assertNotNull(kenaiRealm.getAuthenticationInfo(
          new UsernamePasswordToken("unknown-user-foo-bar", "invalid")));
      Assert.fail("Expected: AccountException to be thrown");
    }
    catch (AccountException e) {
      // expected
    }

    try {
      kenaiRealm.getAuthenticationInfo(new UsernamePasswordToken("random", "JUNK-PASS"));
      Assert.fail("Expected: AccountException to be thrown");
    }
    catch (AccountException e) {
      // expected
    }

    Assert.assertNotNull(kenaiRealm.getAuthenticationInfo(new UsernamePasswordToken(username, password)));

    try {
      kenaiRealm.getAuthenticationInfo(new UsernamePasswordToken("random", "JUNK-PASS"));
      Assert.fail("Expected: AccountException to be thrown");
    }
    catch (AccountException e) {
      // expected
    }
  }
}
