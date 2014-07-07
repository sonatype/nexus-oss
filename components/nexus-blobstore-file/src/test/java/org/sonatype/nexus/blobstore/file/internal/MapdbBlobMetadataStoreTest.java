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
package org.sonatype.nexus.blobstore.file.internal;

import java.nio.file.Path;

import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.file.BlobMetadata;
import org.sonatype.nexus.blobstore.file.BlobState;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link MapdbBlobMetadataStore}.
 */
public class MapdbBlobMetadataStoreTest
  extends TestSupport
{
  private MapdbBlobMetadataStore underTest;

  @Before
  public void setUp() throws Exception {
    Path root = util.createTempDir().toPath();
    this.underTest = new MapdbBlobMetadataStore(root);
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    if (underTest != null) {
      underTest.stop();
    }
  }

  @Test
  public void basic() throws Exception {
    BlobMetadata md = new BlobMetadata(BlobState.CREATING, ImmutableMap.of("foo", "bar"));
    log(md);
    log(underTest.getMetadataMetrics());

    // add a record
    BlobId id = underTest.add(md);
    log(id);
    log(underTest.getMetadataMetrics());

    dumpStates();

    // update a record
    md.setBlobState(BlobState.ALIVE);
    underTest.update(id, md);
    log(underTest.getMetadataMetrics());

    dumpStates();

    // fetch a record
    BlobMetadata md2 = underTest.get(id);
    log(md2);
    log(underTest.getMetadataMetrics());

    // delete a record
    underTest.delete(id);
    log(underTest.getMetadataMetrics());

    dumpStates();

    // compact
    underTest.compact();
    log(underTest.getMetadataMetrics());
  }

  private void dumpStates() {
    for (BlobState state : BlobState.values()) {
      log(state);
      for (BlobId foundId : underTest.findWithState(state)) {
        log("  {}", foundId);
      }
    }
  }
}
