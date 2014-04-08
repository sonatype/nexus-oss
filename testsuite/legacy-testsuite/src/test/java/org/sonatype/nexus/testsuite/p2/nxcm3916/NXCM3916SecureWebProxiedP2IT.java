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
package org.sonatype.nexus.testsuite.p2.nxcm3916;

import java.net.URL;
import java.util.Map;

import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.testsuite.p2.AbstractNexusProxyP2IT;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.impl.MonitorableProxyServlet;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers.exists;

public class NXCM3916SecureWebProxiedP2IT
    extends AbstractNexusProxyP2IT
{

  private static String baseProxyURL;

  public MonitorableProxyServlet monitorableProxyServlet;

  @Rule
  public ServerResource httpProxy = new ServerResource(buildHttpProxyServerProvider());

  static {
    baseProxyURL = TestProperties.getString("proxy.repo.base.url");
  }

  public NXCM3916SecureWebProxiedP2IT() {
    super("nxcm3916");
  }

  protected ServerProvider buildHttpProxyServerProvider() {
    final Map<String, String> users = Maps.newHashMap();
    users.put("admin", "123");
    this.monitorableProxyServlet = new MonitorableProxyServlet(true, users);
    return Server.withPort(TestProperties.getInteger("webproxy-server-port"))
        .serve("/*").withServlet(monitorableProxyServlet)
        .getServerProvider();
  }

  @Before
  public void startWebProxy() throws Exception {

    // ensuring the proxy is working!!!
    assertThat(
        downloadFile(
            new URL(baseProxyURL + "nxcm3916/artifacts.xml"),
            "./target/downloads/nxcm3916/artifacts.xml.temp"
        ),
        exists()
    );
  }

  @Test
  public void test()
      throws Exception
  {
    installAndVerifyP2Feature();

    assertThat(
        monitorableProxyServlet.getAccessedUris(),
        hasItem(baseProxyURL + "nxcm3916/features/com.sonatype.nexus.p2.its.feature_1.0.0.jar")
    );

    assertThat(
        monitorableProxyServlet.getAccessedUris(),
        hasItem(baseProxyURL + "nxcm3916/plugins/com.sonatype.nexus.p2.its.bundle_1.0.0.jar")
    );
  }

}
