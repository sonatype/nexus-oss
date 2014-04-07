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
package org.sonatype.security.realms.kenai;

import com.sonatype.security.realms.kenai.config.model.Configuration;

import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.security.realms.kenai.config.KenaiRealmConfiguration;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.fluent.Server;

import org.junit.Rule;

public abstract class AbstractKenaiRealmTest
    extends NexusAppTestSupport
{

  protected final String username = "test-user";

  protected final String password = "test-user123";

  protected final static String DEFAULT_ROLE = "default-url-role";

  protected static final String AUTH_APP_NAME = "auth_app";

  @Rule
  public ServerResource server = new ServerResource(Server.server().serve("/api/login/*").withServlet(new KenaiMockAuthcServlet()).getServerProvider());

  protected KenaiRealmConfiguration getKenaiRealmConfiguration()
      throws Exception
  {
    // configure Kenai Realm
    KenaiRealmConfiguration kenaiRealmConfiguration = lookup(KenaiRealmConfiguration.class);
    Configuration configuration = kenaiRealmConfiguration.getConfiguration();
    configuration.setDefaultRole(DEFAULT_ROLE);
    configuration.setEmailDomain("sonatype.org");
    configuration.setBaseUrl(server.getServerProvider().getUrl() + AUTH_APP_NAME + "/"); // add the '/' to the end
    // kenaiRealmConfiguration.updateConfiguration( configuration );
    return kenaiRealmConfiguration;
  }
}
