package com.sonatype.nexus.repository.nuget.internal;

import java.util.HashMap;

import com.sonatype.nexus.repository.nuget.internal.odata.ODataTemplates;

import org.sonatype.nexus.repository.search.ComponentMetadataFactory;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.util.NestedAttributesMap;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class NugetGalleryFacetEntryTest
    extends TestSupport
{
  @Test
  public void packageEntrySmokeTest() throws Exception {
    final String packageId = "screwdriver";
    final String version = "0.1.1";

    final NugetGalleryFacetImpl galleryFacet = spy(new NugetGalleryFacetImpl(mock(ComponentMetadataFactory.class)));

    final StorageTx tx = mock(StorageTx.class);
    doReturn(tx).when(galleryFacet).openStorageTx();

    final OrientVertex component = mock(OrientVertex.class);

    // Wire the mock vertices together: component has asset, asset has blobRef
    doReturn(component).when(galleryFacet).findComponent(tx, packageId, version);
    doReturn(mock(NestedAttributesMap.class)).when(galleryFacet).nugetAttribs(tx, component);

    final HashMap<String, ?> data = Maps.newHashMap();
    doReturn(data).when(galleryFacet).toData(any(NestedAttributesMap.class),
        anyMapOf(String.class, String.class));

    galleryFacet.entry("base", packageId, version);

    verify(galleryFacet).interpolateTemplate(ODataTemplates.NUGET_ENTRY, data);
  }
}