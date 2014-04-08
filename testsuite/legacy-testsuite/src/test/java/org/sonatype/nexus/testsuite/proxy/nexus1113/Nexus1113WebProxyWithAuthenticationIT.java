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
package org.sonatype.nexus.testsuite.proxy.nexus1113;

import java.io.File;
import java.util.Map;

import org.sonatype.nexus.integrationtests.ITGroups.PROXY;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.testsuite.proxy.AbstractNexusWebProxyIntegrationTest;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.impl.MonitorableProxyServlet;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class Nexus1113WebProxyWithAuthenticationIT
    extends AbstractNexusWebProxyIntegrationTest
{
  protected ServerProvider buildHttpProxyServerProvider() {
    final Map<String, String> users = Maps.newHashMap();
    users.put("admin", "123");
    this.monitorableProxyServlet = new MonitorableProxyServlet(true, users);
    return Server.withPort(TestProperties.getInteger("webproxy-server-port"))
        .serve("/*").withServlet(monitorableProxyServlet)
        .getServerProvider();
  }

  @Test
  @Category(PROXY.class)
  public void downloadArtifactOverWebProxy()
      throws Exception
  {
    File pomFile = this.getLocalFile("release-proxy-repo-1", "nexus1113", "artifact", "1.0", "pom");
    File pomArtifact =
        this.downloadArtifact("nexus1113", "artifact", "1.0", "pom", null, "target/downloads/nexus1113");
    Assert.assertTrue(FileTestingUtils.compareFileSHA1s(pomArtifact, pomFile));

    File jarFile = this.getLocalFile("release-proxy-repo-1", "nexus1113", "artifact", "1.0", "jar");
    File jarArtifact =
        this.downloadArtifact("nexus1113", "artifact", "1.0", "jar", null, "target/downloads/nexus1113");
    Assert.assertTrue(FileTestingUtils.compareFileSHA1s(jarArtifact, jarFile));

    String artifactUrl = baseProxyURL + "release-proxy-repo-1/nexus1113/artifact/1.0/artifact-1.0.jar";
    Assert.assertTrue("Proxy was not accessed", monitorableProxyServlet.getAccessedUris().contains(artifactUrl));
  }
}
