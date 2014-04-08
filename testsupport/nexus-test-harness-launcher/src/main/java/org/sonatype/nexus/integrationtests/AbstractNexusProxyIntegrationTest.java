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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.fluent.Server;

import org.apache.commons.io.FileUtils;
import org.apache.maven.index.artifact.Gav;
import org.junit.Rule;
import org.restlet.data.MediaType;

public abstract class AbstractNexusProxyIntegrationTest
    extends AbstractNexusIntegrationTest
{
  @Rule
  public ServerResource serverResource = new ServerResource(
      buildServerProvider()
  );

  protected String baseProxyURL = null;

  protected String localStorageDir = null;

  protected Integer proxyPort;

  protected final RepositoryMessageUtil repositoryUtil;

  protected AbstractNexusProxyIntegrationTest() {
    this("release-proxy-repo-1");
  }

  protected AbstractNexusProxyIntegrationTest(String testRepositoryId) {
    super(testRepositoryId);

    this.baseProxyURL = TestProperties.getString("proxy.repo.base.url");
    this.localStorageDir = TestProperties.getString("proxy.repo.base.dir");
    this.proxyPort = TestProperties.getInteger("proxy.server.port");

    this.repositoryUtil = new RepositoryMessageUtil(this, getXMLXStream(), MediaType.APPLICATION_XML);
  }

  protected ServerProvider buildServerProvider() {
    return Server.withPort(TestProperties.getInteger("proxy.server.port"))
        .serve("/remote/*").fromDirectory(new File(TestProperties.getString("proxy-repo-target-dir")))
        .getServerProvider();
  }

  public File getLocalFile(String repositoryId, Gav gav) {
    return this.getLocalFile(repositoryId, gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
        gav.getExtension());
  }

  public File getLocalFile(String repositoryId, String groupId, String artifact, String version, String type) {
    File result =
        new File(this.localStorageDir, repositoryId + "/" + groupId.replace('.', '/') + "/" + artifact + "/"
            + version + "/" + artifact + "-" + version + "." + type);
    log.debug("Returning file: " + result);
    return result;
  }

  @Override
  protected void copyTestResources() throws IOException {
    super.copyTestResources();

    final File dest = new File(localStorageDir);

    if (dest.exists()) {
      FileUtils.forceDelete(dest);
    }

    File source = getTestResourceAsFile("proxy-repo");
    if (source == null || !source.exists()) {
      return;
    }

    FileTestingUtils.interpolationDirectoryCopy(source, dest, getTestProperties());
  }
}
