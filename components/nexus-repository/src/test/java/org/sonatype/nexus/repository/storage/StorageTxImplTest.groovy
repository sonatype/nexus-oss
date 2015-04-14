/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.storage

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import org.junit.Test
import org.mockito.Mock
import org.sonatype.nexus.blobstore.api.BlobRef
import org.sonatype.nexus.common.collect.NestedAttributesMap
import org.sonatype.nexus.common.hash.HashAlgorithm
import org.sonatype.nexus.repository.IllegalOperationException
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static org.hamcrest.MatcherAssert.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

/**
 * Tests for {@link StorageTxImpl}.
 */
class StorageTxImplTest
extends TestSupport
{
  @Mock
  private BlobTx blobTx
  @Mock
  private ODatabaseDocumentTx db
  @Mock
  private Bucket bucket
  @Mock
  private BucketEntityAdapter bucketEntityAdapter
  @Mock
  private ComponentEntityAdapter componentEntityAdapter
  @Mock
  private AssetEntityAdapter assetEntityAdapter
  @Mock
  private Asset asset
  @Mock
  private InputStream inputStream
  private Map<String, String> headers = [:]
  private Iterable<HashAlgorithm> hashAlgorithms = []

  /**
   * Given:
   * - an asset with a blob
   * - DENY write policy
   * When:
   * - asset is removed
   * Then:
   * - exception is thrown
   * - blob is not removed from db
   * - asset is not removed from db
   */
  @Test
  void 'deleting assets fails when DENY write policy'() {
    when(asset.blobRef()).thenReturn(mock(BlobRef))
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.DENY, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    try {
      underTest.deleteAsset(asset)
      assertThat 'Expected IllegalOperationException', false
    }
    catch (IllegalOperationException e) {}
    verify(blobTx, never()).delete(any(BlobRef))
    verify(assetEntityAdapter, never()).delete(db, asset)
  }

  /**
   * Given:
   * - an asset without a blob
   * - DENY write policy
   * When:
   * - asset is removed
   * Then:
   * - asset is removed from db
   */
  @Test
  void 'deleting assets pass when DENY write policy without blob'() {
    new StorageTxImpl(blobTx, db, bucket, WritePolicy.DENY, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter).deleteAsset(asset)
    verify(assetEntityAdapter, times(1)).delete(db, asset)
  }

  /**
   * Given:
   * - an asset with a blob
   * - no (NULL) write policy
   * When:
   * - asset is removed
   * Then:
   * - blob is removed from db
   * - asset is removed from db
   */
  @Test
  void 'deleting assets pass when NULL write policy'() {
    deleteAssetWhenWritePolicy(null)
  }

  /**
   * Given:
   * - an asset with a blob
   * - ALLOW write policy
   * When:
   * - asset is removed
   * Then:
   * - blob is removed from db
   * - asset is removed from db
   */
  @Test
  void 'deleting assets pass when ALLOW write policy'() {
    deleteAssetWhenWritePolicy(WritePolicy.ALLOW)
  }

  /**
   * Given:
   * - an asset with a blob
   * - ALLOW_ONCE write policy
   * When:
   * - asset is removed
   * Then:
   * - blob is removed from db
   * - asset is removed from db
   */
  @Test
  void 'deleting assets pass when ALLOW_ONCE write policy'() {
    deleteAssetWhenWritePolicy(WritePolicy.ALLOW_ONCE)
  }

  void deleteAssetWhenWritePolicy(final WritePolicy writePolicy) {
    def blobRef = mock(BlobRef)
    when(asset.blobRef()).thenReturn(blobRef)
    new StorageTxImpl(blobTx, db, bucket, writePolicy, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter).deleteAsset(asset)
    verify(blobTx, times(1)).delete(blobRef)
    verify(assetEntityAdapter, times(1)).delete(db, asset)
  }

  /**
   * Given:
   * - an asset with a blob
   * - DENY write policy
   * When:
   * - setting a blob on the asset
   * Then:
   * - exception is thrown
   * - existing blob is not removed from db
   * - new blob is not created in db
   * - asset blob reference is not changed
   */
  @Test
  void 'setting blob fails on asset with blob when DENY write policy'() {
    def blobRef = mock(BlobRef)
    when(asset.blobRef()).thenReturn(blobRef)
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.DENY, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    try {
      underTest.setBlob(inputStream, headers, asset, hashAlgorithms, "text/plain")
      assertThat 'Expected IllegalOperationException', false
    }
    catch (IllegalOperationException e) {}
    verify(blobTx, never()).delete(blobRef)
    verify(blobTx, never()).create(inputStream, headers)
    verify(asset, never()).blobRef(any(BlobRef))
  }

  /**
   * Given:
   * - an asset without a blob
   * - DENY write policy
   * When:
   * - setting a blob on the asset
   * Then:
   * - exception is thrown
   * - new blob is not created in db
   * - asset blob reference is not changed
   */
  @Test
  void 'setting blob fails on asset without blob when DENY write policy'() {
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.DENY, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    try {
      underTest.setBlob(inputStream, headers, asset, hashAlgorithms, "text/plain")
      assertThat 'Expected IllegalOperationException', false
    }
    catch (IllegalOperationException e) {}
    verify(blobTx, never()).delete(any(BlobRef))
    verify(blobTx, never()).create(inputStream, headers)
    verify(asset, never()).blobRef(any(BlobRef))
  }

  /**
   * Given:
   * - an asset with a blob
   * - ALLOW_ONCE write policy
   * When:
   * - setting a blob on the asset
   * Then:
   * - exception is thrown
   * - existing blob is not removed from db
   * - new blob is not created in db
   * - asset blob reference is not changed
   */
  @Test
  void 'setting blob fails on asset with blob when ALLOW_ONCE write policy'() {
    def blobRef = mock(BlobRef)
    when(asset.blobRef()).thenReturn(blobRef)
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.ALLOW_ONCE, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    try {
      underTest.setBlob(inputStream, headers, asset, hashAlgorithms, "text/plain")
      assertThat 'Expected IllegalOperationException', false
    }
    catch (IllegalOperationException e) {}
    verify(blobTx, never()).delete(blobRef)
    verify(blobTx, never()).create(inputStream, headers)
    verify(asset, never()).blobRef(any(BlobRef))
  }

  /**
   * Given:
   * - an asset without a blob
   * - ALLOW_ONCE write policy
   * When:
   * - setting a blob on the asset
   * Then:
   * - new blob is created in db
   * - asset blob reference is changed
   */
  @Test
  void 'setting blob pass on asset without blob when ALLOW_ONCE write policy'() {
    when(asset.attributes()).thenReturn(mock(NestedAttributesMap))
    def newBlobRef = mock(BlobRef)
    when(blobTx.create(any(InputStream), eq(headers))).thenReturn(newBlobRef)
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.ALLOW_ONCE, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    underTest.setBlob(inputStream, headers, asset, hashAlgorithms, "text/plain")
    verify(asset, times(1)).blobRef(newBlobRef)
  }

  /**
   * Given:
   * - an asset with a blob
   * - ALLOW write policy
   * When:
   * - setting a blob on the asset
   * Then:
   * - existing blob is removed from db
   * - new blob is created in db
   * - asset blob reference is changed
   */
  @Test
  void 'setting blob pass on asset with blob when ALLOW write policy'() {
    def blobRef = mock(BlobRef)
    when(asset.blobRef()).thenReturn(blobRef)
    when(asset.attributes()).thenReturn(mock(NestedAttributesMap))
    def newBlobRef = mock(BlobRef)
    when(blobTx.create(any(InputStream), eq(headers))).thenReturn(newBlobRef)
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.ALLOW, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    underTest.setBlob(inputStream, headers, asset, hashAlgorithms, "text/plain")
    verify(blobTx, times(1)).delete(blobRef)
    verify(asset, times(1)).blobRef(newBlobRef)
  }

  /**
   * Given:
   * - an asset without a blob
   * - ALLOW write policy
   * When:
   * - setting a blob on the asset
   * Then:
   * - new blob is created in db
   * - asset blob reference is changed
   */
  @Test
  void 'setting blob pass on asset without blob when ALLOW write policy'() {
    when(asset.attributes()).thenReturn(mock(NestedAttributesMap))
    def newBlobRef = mock(BlobRef)
    when(blobTx.create(any(InputStream), eq(headers))).thenReturn(newBlobRef)
    def underTest = new StorageTxImpl(blobTx, db, bucket, WritePolicy.ALLOW, bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter)
    underTest.setBlob(inputStream, headers, asset, hashAlgorithms, "text/plain")
    verify(asset, times(1)).blobRef(newBlobRef)
  }

}
