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

package org.sonatype.nexus.proxy;

import java.io.File;
import java.util.List;

import org.sonatype.sisu.litmus.testsupport.TestUtil;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.filesystem.Get;
import org.sonatype.tests.http.server.jetty.behaviour.filesystem.Head;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * The Class JettyTestsuiteEnvironment.
 *
 * @author cstamas
 */
public abstract class AbstractJettyEnvironmentBuilder
    implements EnvironmentBuilder
{
  private final Server server;

  private final TestUtil testUtil;

  private List<String> repoIds;

  public AbstractJettyEnvironmentBuilder(List<String> reposes) {
    this.server = Server.server();
    this.testUtil = new TestUtil(this);
    this.repoIds = Lists.newArrayList();
    if (reposes != null && !reposes.isEmpty()) {
      repoIds.addAll(reposes);
    }
    createRemoteServers();
  }

  @Override
  public void startService()
      throws Exception
  {
    server.start();
  }

  @Override
  public void stopService()
      throws Exception
  {
    server.stop();
  }

  protected Server server() {
    return server;
  }

  protected List<String> repoIds() { return repoIds; }

  protected void createRemoteServers() {
    for (String repoId : repoIds) {
      createRemoteServer(repoId);
    }
  }

  /**
   * Creates a remote server delivering content from known places
   */
  protected void createRemoteServer(final String repoId) {
    final File repoRoot = testUtil.resolveFile("target/test-classes/" + repoId);
    checkArgument(repoRoot.isDirectory(), "Repository not exists: " + repoRoot.getAbsolutePath());
    server().serve("/" + repoId + "/*").withBehaviours(new Get(repoRoot.getAbsolutePath()), new Head(repoRoot.getAbsolutePath()));
  }
}
