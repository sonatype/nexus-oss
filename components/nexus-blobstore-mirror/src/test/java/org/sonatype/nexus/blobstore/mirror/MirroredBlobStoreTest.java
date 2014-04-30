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
package org.sonatype.nexus.blobstore.mirror;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Random;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.HashingSubdirFileLocationPolicy;
import org.sonatype.nexus.blobstore.file.JsonHeaderFormat;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.locking.DummyLockFactory;
import org.sonatype.nexus.blobstore.locking.LockingBlobStore;
import org.sonatype.nexus.blobstore.support.UuidBlobIdFactory;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.0
 */
public class MirroredBlobStoreTest
    extends TestSupport
{
  private Map<String, String> testHeaders;

  private byte[] testData;

  @Before
  public void setUp() {
    testHeaders = ImmutableMap.of("name", "children/artificial/pinocchio");
    testData = new byte[10000];
    new Random().nextBytes(testData);
  }

  @Test
  public void test() throws IOException {
    final BlobStore store1 = createFileBlobStore();
    final BlobStore store2 = createFileBlobStore();

    final BlobStore store3 = createFileBlobStore();
    final BlobStore store4 = createFileBlobStore();

    final MirroredBlobStore mirror1 = new MirroredBlobStore("A", store3, store1);
    final MirroredBlobStore mirror2 = new MirroredBlobStore("B", store4, store2);

    final BlobStore root = new LockingBlobStore(new MirroredBlobStore("Root", mirror1, mirror2),
        new DummyLockFactory());

    final Blob blob = root.create(new ByteArrayInputStream(testData), testHeaders);

    root.get(blob.getId());

    root.delete(blob.getId());
  }

  private FileBlobStore createFileBlobStore() throws IOException {
    final FileBlobStore store = new FileBlobStore(Files.createTempDirectory("blobstore"), null);
    store.setFileOperations(new SimpleFileOperations());
    store.setHeaderFormat(new JsonHeaderFormat());
    store.setFileLocationPolicy(new HashingSubdirFileLocationPolicy());
    store.setBlobIdFactory(new UuidBlobIdFactory());
    return store;
  }
}
