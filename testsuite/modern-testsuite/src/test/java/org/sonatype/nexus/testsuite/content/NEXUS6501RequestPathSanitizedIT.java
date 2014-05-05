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

package org.sonatype.nexus.testsuite.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.*;

/**
 * IT for NEXUS-6501.
 * <p/>
 * General note: due to filters and use of request path in them, basically the response we get is same like in cases
 * when the path cannot be extracted from path: 401, while the desired response might be better 400.
 */
public class NEXUS6501RequestPathSanitizedIT
    extends ContentITSupport
{

  public NEXUS6501RequestPathSanitizedIT(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Test
  public void properPath() throws IOException {
    // the path is okay but does not exists
    assertThat(makeRequestAndReturnResponseCode("/nexus/content/releases/some/proper/content/foo.txt"), equalTo(404));
  }

  @Test
  public void corruptPathCreationMaven()
      throws Exception
  {
    // path is wrong, 401 due to Nx filter trying to figure out security, and ResourceStoreRequest needed for that
    assertThat(makeRequestAndReturnResponseCode("/nexus/content/releases/some/path/${maven.placeholder}/bar"),
        equalTo(401));
  }

  @Test
  public void corruptPathCreationNexus6501_case1()
      throws Exception
  {
    // path is wrong, even forms bad HTTP request so Jetty pukes on this
    assertThat(makeRequestAndReturnResponseCode(
        "/nexus/content/releases/org/springframework/spring-context/maven-metadata.xml (4 KB at 2.5 KB/sec)"),
        equalTo(400));
  }

  @Test
  public void corruptPathCreationNexus6501_case1_a()
      throws Exception
  {
    // path is wrong, 401 due to Nx filter trying to figure out security, and ResourceStoreRequest needed for that
    assertThat(makeRequestAndReturnResponseCode(
            "/nexus/content/releases/org/springframework/spring-context/maven-metadata.xml%20(4%20KB%20at%202.5%20KB/sec)"),
        equalTo(401));
  }

  @Test
  public void corruptPathCreationNexus6501_case2()
      throws Exception
  {
    // path is wrong, 401 due to Nx filter trying to figure out security, and ResourceStoreRequest needed for that
    assertThat(makeRequestAndReturnResponseCode(
        "/nexus/content/releases/co/orange/crm/paym/services/ear/crm-paym-subscription-contract-ear/${env.CCASE_VERSION_STRING}/crm-paym-subscription-contract-ear-${env.CCASE_VERSION_STRING}.pom"),
        equalTo(401));
  }

  // ==

  /**
   * Manually makes a HTTP request (as if using any HTTP library this would crack on client side due to illegal
   * characters in path.
   */
  private int makeRequestAndReturnResponseCode(final String path) throws IOException {
    try (final Socket socket = new Socket(InetAddress.getByName(nexus().getUrl().getHost()), nexus().getPort())) {
      final PrintWriter pw = new PrintWriter(socket.getOutputStream());
      pw.println("GET " + path + " HTTP/1.1");
      pw.println("Host: " + nexus().getUrl().getHost());
      pw.println();
      pw.flush();
      final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String response = br.readLine();
      // this line is in form of
      // HTTP/1.1 XXX RRRRRRRRR
      return Integer.valueOf(response.substring(9, 12));
    }
  }
}
