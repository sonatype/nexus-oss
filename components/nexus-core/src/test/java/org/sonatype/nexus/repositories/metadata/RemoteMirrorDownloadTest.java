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

package org.sonatype.nexus.repositories.metadata;

import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.sisu.litmus.testsupport.TestUtil;
import org.sonatype.tests.http.runner.junit.ServerResource;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.filesystem.Get;

import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class RemoteMirrorDownloadTest
    extends NexusAppTestSupport
{
  @Rule
  public ServerResource server = new ServerResource(Server.server()
      .serve("/repo-with-mirror/*")
      .withBehaviours(Get.get(util.resolveFile("target/test-classes/repo-with-mirror")))
      .getServerProvider());

  @Test
  public void testRemoteMetadataDownload() throws Exception {
    final NexusRepositoryMetadataHandler repoMetadata = lookup(NexusRepositoryMetadataHandler.class);
    String url = server.getServerProvider().getUrl() + "/repo-with-mirror/";
    final RepositoryMetadata metadata = repoMetadata.readRemoteRepositoryMetadata(url);
    assertThat(metadata, notNullValue());
    assertThat(metadata.getMirrors(), hasSize(2));
    assertThat(metadata.getMirrors().get(0).getId(), equalTo("mirror1"));
    assertThat(metadata.getMirrors().get(1).getId(), equalTo("mirror2"));
  }
}
