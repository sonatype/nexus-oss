/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal;

import java.io.InputStream;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.blobstore.api.BlobMetrics;
import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.StreamPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests that nuget gallery 'get' behaves correctly.
 */
public class NugetGalleryFacetImplGetTest
{
  @Test
  public void testGetNonexistentPackage() throws Exception {
    final String version = "2.1.1";
    final String packageId = "jQuery";

    final NugetGalleryFacetImpl galleryFacet = spy(new NugetGalleryFacetImpl());

    doReturn(null).when(galleryFacet).findComponent(any(StorageTx.class), eq(packageId), eq(version));

    UnitOfWork.beginBatch(mock(StorageTx.class));
    final Payload payload;
    try {
      payload = galleryFacet.get(packageId, version);
    }
    finally {
      UnitOfWork.pause();
    }

    assertThat(payload, is(nullValue()));
  }

  @Test
  public void testPayloadMadeFromBlob() throws Exception {
    final NugetGalleryFacetImpl galleryFacet = spy(new NugetGalleryFacetImpl());

    final String contentType = "application/zip";
    final long size = 2000000L;

    final String version = "2.1.1";
    final String packageId = "jQuery";

    final BlobRef blobRef = new BlobRef("a", "b", "c");
    final Blob blob = mock(Blob.class);
    final BlobMetrics blobMetrics = mock(BlobMetrics.class);
    when(blob.getMetrics()).thenReturn(blobMetrics);
    when(blobMetrics.getContentSize()).thenReturn(size);
    final InputStream blobStream = mock(InputStream.class);

    final Asset asset = mock(Asset.class);
    final Component component = mock(Component.class);

    final StorageTx tx = mock(StorageTx.class);

    doReturn(component).when(galleryFacet).findComponent(any(StorageTx.class), eq(packageId), eq(version));
    when(tx.firstAsset(component)).thenReturn(asset);
    when(asset.contentType()).thenReturn(contentType);
    when(asset.blobRef()).thenReturn(blobRef);
    when(tx.requireBlob(eq(blobRef))).thenReturn(blob);
    when(blob.getInputStream()).thenReturn(blobStream);

    UnitOfWork.beginBatch(tx);
    final Payload payload;
    try {
      payload = galleryFacet.get(packageId, version);
    }
    finally {
      UnitOfWork.end();
    }

    assertTrue(payload instanceof StreamPayload);
    StreamPayload streamPayload = (StreamPayload) payload;

    assertThat(streamPayload.openInputStream(), is(blobStream));
    assertThat(streamPayload.getSize(), is(size));
    assertThat(streamPayload.getContentType(), is(contentType));
  }
}
