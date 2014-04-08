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
package org.sonatype.nexus.testsuite.p2;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.api.ServerProvider;
import org.sonatype.tests.http.server.fluent.Server;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;

import static org.sonatype.nexus.test.utils.FileTestingUtils.interpolationDirectoryCopy;

public abstract class AbstractNexusProxyP2IT
    extends AbstractNexusP2IT
{
  @Rule
  public ServerResource serverResource = new ServerResource(buildServerProvider());

  protected static final String localStorageDir;

  static {
    localStorageDir = TestProperties.getString("proxy.repo.base.dir");
  }

  protected AbstractNexusProxyP2IT() {
    super();
  }

  protected AbstractNexusProxyP2IT(final String testRepositoryId) {
    super(testRepositoryId);
  }

  protected ServerProvider buildServerProvider() {
    return Server.withPort(TestProperties.getInteger("proxy.server.port"))
        .serve("/remote/*").fromDirectory(new File(TestProperties.getString("proxy-repo-target-dir")))
        .getServerProvider();
  }

  protected void replaceInFile(final String filename, final String target, final String replacement)
      throws IOException
  {
    String content = FileUtils.readFileToString(new File(filename));
    content = content.replace(target, replacement);
    FileUtils.write(new File(filename), content);
  }

  @Override
  protected void copyTestResources()
      throws IOException
  {
    super.copyTestResources();

    final File dest = new File(localStorageDir);

    if (dest.exists()) {
      FileUtils.forceDelete(dest);
    }

    File source = getTestResourceAsFile("proxy-repo");
    if (source == null || !source.exists()) {
      return;
    }

    interpolationDirectoryCopy(source, dest, TestProperties.getAll());
  }

}
