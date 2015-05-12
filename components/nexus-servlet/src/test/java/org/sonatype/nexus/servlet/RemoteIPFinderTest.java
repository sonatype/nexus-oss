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
package org.sonatype.nexus.servlet;

import javax.servlet.http.HttpServletRequest;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.net.HttpHeaders.X_FORWARDED_FOR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

/**
 * Tests for {@link RemoteIPFinder}.
 */
public class RemoteIPFinderTest
  extends TestSupport
{
  @Test
  @Ignore("Fails when not running on VPN; ignoring for now")
  public void testResolveIP() {
    HttpServletRequest http = Mockito.mock(HttpServletRequest.class);

    doReturn("").when(http).getHeader(X_FORWARDED_FOR);
    assertEquals(null, RemoteIPFinder.findIP(http));

    doReturn(null).when(http).getHeader(X_FORWARDED_FOR);
    assertEquals(null, RemoteIPFinder.findIP(http));

    doReturn("127.0.0.1").when(http).getHeader(X_FORWARDED_FOR);
    assertEquals("127.0.0.1", RemoteIPFinder.findIP(http));

    // NOTE: that if you use a DNS provider, such as OpenDNS, or internet cafe which buckets non-resolvable host names
    // NOTE: ... to a landing page host, these tests will fail when the name 'missing' actually resolves to an IP
    doReturn("missing, 127.0.0.2, unknown, 127.0.0.1").when(http).getHeader(X_FORWARDED_FOR);
    assertEquals("127.0.0.2", RemoteIPFinder.findIP(http));

    doReturn("127.0.0.3, 127.0.0.2, 127.0.0.1").when(http).getHeader(X_FORWARDED_FOR);
    assertEquals("127.0.0.3", RemoteIPFinder.findIP(http));

    doReturn("localhost").when(http).getHeader(X_FORWARDED_FOR);
    assertEquals("localhost", RemoteIPFinder.findIP(http));

    doReturn("client, proxy1, proxy2").when(http).getHeader(X_FORWARDED_FOR);
    doReturn(null).when(http).getRemoteAddr();
    assertEquals(null, RemoteIPFinder.findIP(http));

    doReturn("client, proxy1, proxy2").when(http).getHeader(X_FORWARDED_FOR);
    doReturn("upstream").when(http).getRemoteAddr();
    assertEquals("upstream", RemoteIPFinder.findIP(http));
  }
}
