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
package org.sonatype.nexus.testsuite.security;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class SecureSessionCookieIT
    extends NexusHttpsITSupport
{

  public SecureSessionCookieIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    return super.configureNexus(configuration).setSystemProperty("shiro.secureSessionCookies", "true");
  }

  /**
   * Tests secure session cookie behaviour.
   */
  @Test
  public void sessionCookie() throws Exception {
    HttpGet get = new HttpGet(getSecureUrl() + "service/local/authentication/login");
    try (CloseableHttpClient client = clientBuilder().build()) {
      try (CloseableHttpResponse response = client.execute(get, clientContext())) {
        assertThat(response.getStatusLine().getStatusCode(), is(200));
      }
      assertThat(getSessionCookie().isSecure(), is(true));
    }
  }

}
