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

package org.sonatype.nexus.testsuite.repo.nexus1197;

import java.io.FileNotFoundException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.fluent.Server;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class Nexus1197CheckUserAgentIT
    extends AbstractNexusIntegrationTest
{
  private static class UserAgentSniffer
      implements Behaviour
  {

    public String userAgent;

    @Override
    public boolean execute(final HttpServletRequest request, final HttpServletResponse response,
                           final Map<Object, Object> ctx)
        throws Exception
    {
      userAgent = request.getHeader("User-Agent");
      return true;
    }
  }

  private UserAgentSniffer userAgentSniffer = new UserAgentSniffer();

  @Rule
  public ServerResource server = new ServerResource(
      Server.withPort(TestProperties.getInteger("proxy.server.port"))
          .serve("/*").withBehaviours(userAgentSniffer)
          .getServerProvider()
  );

  public Nexus1197CheckUserAgentIT() {
    super("release-proxy-repo-1");
  }

  @Test
  public void downloadArtifactOverWebProxy()
      throws Exception
  {
    try {
      downloadArtifact("nexus1197", "artifact", "1.0", "pom", null, "target/downloads");
    }
    catch (FileNotFoundException e) {
      // ok, just ignore
    }

    // Nexus/1.2.0-beta-2-SNAPSHOT (OSS; Windows XP; 5.1; x86; 1.6.0_07)
    // apacheHttpClient3x/1.2.0-beta-2-SNAPSHOT Nexus/1.0
    final String userAgent = userAgentSniffer.userAgent;

    assertThat(userAgent, notNullValue());
    assertThat(userAgent, startsWith("Nexus/"));
    assertThat(userAgent, anyOf(containsString("(OSS"), containsString("(PRO")));
  }
}
