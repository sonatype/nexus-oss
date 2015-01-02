/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.kenai.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.security.realms.kenai.Kenai;
import org.sonatype.security.realms.kenai.KenaiConfiguration;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.common.base.Throwables;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.junit.Rule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Kenai test super class. Note: NexusAppTestSupport needed as these tests boot whole security up.
 */
public abstract class AbstractKenaiRealmTest
    extends NexusAppTestSupport
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
    modules.add(new Module()
    {
      @Override
      public void configure(final Binder binder) {
        binder.bind(Kenai.class).toInstance(mockKenai());
      }
    });
  }

  @Override
  protected void customizeContainerConfiguration(final ContainerConfiguration configuration) {
    super.customizeContainerConfiguration(configuration);
    configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX);
  }

  protected Kenai mockKenai() {
    KenaiConfiguration kenaiConfiguration = new KenaiConfiguration();
    try {
      kenaiConfiguration.setBaseUrl(server.getServerProvider().getUrl() + AUTH_APP_NAME + "/");
    }
    catch (MalformedURLException e) {
      throw Throwables.propagate(e);
    }
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
}
