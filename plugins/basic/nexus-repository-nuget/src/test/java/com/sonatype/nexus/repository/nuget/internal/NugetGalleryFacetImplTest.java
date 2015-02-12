package com.sonatype.nexus.repository.nuget.internal;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.sonatype.nexus.common.time.Clock;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.mockito.Mockito;

import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_CREATED;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_DOWNLOAD_COUNT;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_LAST_UPDATED;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_PUBLISHED;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_VERSION_DOWNLOAD_COUNT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    verify(galleryFacet).maintainAggregateInfo(tx, "SONATYPE.TEST");
  }

  @Test
  public void derivedAttributesSetForNewComponents() {
    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());
    final Clock clock = new TestableClock();
    galleryFacet.clock = clock;

    final Map<String, String> incomingMap = Maps.newHashMap();
    final Map<String, Object> storedMap = Maps.newHashMap();
    galleryFacet.setDerivedAttributes(incomingMap, new NestedAttributesMap("NUGET", storedMap), false);

    assertThat(((Date) storedMap.get(P_CREATED)).getTime(), is(clock.millis()));
    assertThat(((Date) storedMap.get(P_PUBLISHED)).getTime(), is(clock.millis()));
    assertThat(((Date) storedMap.get(P_LAST_UPDATED)).getTime(), is(clock.millis()));
    assertThat((int) storedMap.get(P_DOWNLOAD_COUNT), is(0));
    assertThat((int) storedMap.get(P_VERSION_DOWNLOAD_COUNT), is(0));
  }

  @Test
  public void derivedAttributesSetForRepublishedComponents() {
    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());
    final Clock clock = new TestableClock();
    galleryFacet.clock = clock;

    final Map<String, String> incomingMap = Maps.newHashMap();
    final NestedAttributesMap storedAttributes = mock(NestedAttributesMap.class);
    galleryFacet.setDerivedAttributes(incomingMap, storedAttributes, true);

    verify(storedAttributes).set(eq(P_CREATED), eq(clock.dateTime().toDate()));
    verify(storedAttributes).set(eq(P_PUBLISHED), eq(clock.dateTime().toDate()));
    verify(storedAttributes).set(eq(P_LAST_UPDATED), eq(clock.dateTime().toDate()));
    verify(storedAttributes, never()).set(eq(P_DOWNLOAD_COUNT), any());
    verify(storedAttributes, never()).set(eq(P_VERSION_DOWNLOAD_COUNT), any());
  }

  private static class TestableClock
      extends Clock
  {
    @Override
    public long millis() {
      return 42L;
    }
  }
}