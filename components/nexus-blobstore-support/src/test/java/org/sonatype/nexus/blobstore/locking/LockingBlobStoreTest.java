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
package org.sonatype.nexus.blobstore.locking;

import java.io.InputStream;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobId;
import org.sonatype.nexus.blobstore.api.BlobInUseException;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LockingBlobStoreTest
    extends TestSupport
{
  private BlobStore underlyingStore;

  private BlobLockFactory lockProvider;

  private BlobId blobId;

  private Blob underlyingBlob;

  private BlobLock mockLock;

  private LockingBlobStore lockingBlobStore;

  @Before
  public void setUp() {
    underlyingStore = mock(BlobStore.class);
    lockProvider = mock(BlobLockFactory.class);

    blobId = mock(BlobId.class);
    underlyingBlob = mock(Blob.class);

    when(underlyingStore.get(blobId)).thenReturn(underlyingBlob);
    when(underlyingBlob.getInputStream()).thenReturn(mock(InputStream.class));

    when(lockProvider.readLock(blobId)).thenReturn(mock(BlobLock.class));
    when(lockProvider.tryExclusiveLock(blobId)).thenReturn(null);

    lockingBlobStore = new LockingBlobStore(underlyingStore, lockProvider);
  }

  @Test(expected = BlobInUseException.class)
  public void deletionWhileUsingStreamThrowsException() {

    final Blob lockProtectedBlob = lockingBlobStore.get(blobId);

    lockProtectedBlob.getInputStream();

    lockingBlobStore.delete(blobId);
  }

  @Test
  public void hardDeletionSucceedsDespiteLock() {

    final Blob lockProtectedBlob = lockingBlobStore.get(blobId);

    lockProtectedBlob.getInputStream();
    lockingBlobStore.deleteHard(blobId);

    verify(underlyingStore).deleteHard(blobId);
  }

}
