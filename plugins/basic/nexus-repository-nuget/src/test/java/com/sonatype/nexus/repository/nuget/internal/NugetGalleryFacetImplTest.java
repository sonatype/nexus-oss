package com.sonatype.nexus.repository.nuget.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class NugetGalleryFacetImplTest
{
  @Test
  public void putCreatesPackageMetadataAndBlob() throws Exception {
    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());

    final StorageTx tx = mock(StorageTx.class);

    doReturn(tx).when(galleryFacet).openStorageTx();

    final InputStream packageStream = getClass().getResourceAsStream("/SONATYPE.TEST.1.0.nupkg");

    doNothing().when(galleryFacet)
        .createOrUpdatePackage(any(StorageTx.class), any(Map.class), any(InputStream.class));

    doNothing().when(galleryFacet).maintainAggregateInfo(any(StorageTx.class), eq("SONATYPE.TEST"));

    galleryFacet.put(packageStream);
  }

  @Test
  public void derivedAttributesSetForNewComponents() {
    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());

    final HashMap<String, Object> map = Maps.newHashMap();
    galleryFacet.setDerivedAttributes(null, new NestedAttributesMap("NUGET", map), false);
  }

  @Test
  public void derivedAttributesSetForRepublishedComponents() {

  }
}