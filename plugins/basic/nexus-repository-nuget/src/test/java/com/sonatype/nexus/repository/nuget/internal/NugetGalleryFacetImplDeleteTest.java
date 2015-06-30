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

import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.common.entity.EntityMetadata;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;
import org.mockito.Mockito;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies component deletion from a nuget gallery facet.
 */
public class NugetGalleryFacetImplDeleteTest
    extends TestSupport
{

  @Test
  public void deleteRemovesComponentAssetAndBlob() throws Exception {
    final String packageId = "screwdriver";
    final String version = "0.1.1";

    final EventBus eventBus = mock(EventBus.class);
    final Repository repository = mock(Repository.class);
    when(repository.facet(SearchFacet.class)).thenReturn(mock(SearchFacet.class));
    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl()
    {
      @Override
      protected EventBus getEventBus() {
        return eventBus;
      }

      @Override
      protected Repository getRepository() {
        return repository;
      }
    });
    final StorageTx tx = mock(StorageTx.class);
    doReturn(tx).when(galleryFacet).openStorageTx();

    final Component component = mock(Component.class);
    final Asset asset = mock(Asset.class);
    final BlobRef blobRef = mock(BlobRef.class); //new BlobRef("local", "default", "a34af31");
    final EntityMetadata metadata = mock(EntityMetadata.class);

    // Wire the mocks together: component has asset, asset has blobRef
    doReturn(component).when(galleryFacet).findComponent(tx, packageId, version);
    when(tx.browseAssets(component)).thenReturn(asList(asset));
    when(asset.blobRef()).thenReturn(blobRef);
    when(component.getEntityMetadata()).thenReturn(metadata);
    when(metadata.getId()).thenReturn(mock(EntityId.class));

    galleryFacet.delete(packageId, version);

    // Verify that everything got deleted
    verify(tx).deleteComponent(component);
  }
}
