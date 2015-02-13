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
package com.sonatype.nexus.repository.nuget.internal;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;

import org.sonatype.nexus.common.time.Clock;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.util.NestedAttributesMap;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.junit.Test;
import org.mockito.Mockito;

import static com.sonatype.nexus.repository.nuget.internal.NugetFormat.NAME;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_CREATED;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_DOWNLOAD_COUNT;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_IS_ABSOLUTE_LATEST_VERSION;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_IS_LATEST_VERSION;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_IS_PRERELEASE;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_LAST_UPDATED;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_PUBLISHED;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_VERSION;
import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.P_VERSION_DOWNLOAD_COUNT;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  // single pre-release
  // single release

  // mix, latest is pre-release
  // mix, latest is release

  @Test
  public void versionsFlagForSinglePrerelease() {
    final StorageTx tx = mock(StorageTx.class);

    final OrientVertex preRelease = buildVersionMock(tx, "2.1.8-greenbell", true);

    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(preRelease));

    verifyVersionFlags(tx.getAttributes(preRelease).child(NAME), false, true);
  }

  @Test
  public void versionsFlagForSingleRelease() {
    final StorageTx tx = mock(StorageTx.class);

    final OrientVertex release = buildVersionMock(tx, "2.1.8", false);

    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(release));

    verifyVersionFlags(tx.getAttributes(release).child(NAME), true, true);
  }

  @Test
  public void versionsFlagForReleaseIsLatest() {
    final StorageTx tx = mock(StorageTx.class);

    final OrientVertex release = buildVersionMock(tx, "2.1.8", false);
    // TODO: Aether doesn't correctly order 2.1.7-greenbell and 2.1.7
    final OrientVertex preRelease = buildVersionMock(tx, "2.1.7-greenbell", true);

    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(release, preRelease));

    verifyVersionFlags(tx.getAttributes(release).child(NAME), true, true);
    verifyVersionFlags(tx.getAttributes(preRelease).child(NAME), false, false);
  }

  @Test
  public void versionsFlagForPrereleaseIsLatest() {
    final StorageTx tx = mock(StorageTx.class);

    final OrientVertex preRelease = buildVersionMock(tx, "2.1.9-greenbell", true);
    final OrientVertex release = buildVersionMock(tx, "2.1.8", false);

    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl());
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(release, preRelease));

    verifyVersionFlags(tx.getAttributes(preRelease).child(NAME), false, true);
    verifyVersionFlags(tx.getAttributes(release).child(NAME), true, false);
  }

  private OrientVertex buildVersionMock(final StorageTx tx, final String version, final boolean isPrerelease) {
    final OrientVertex vertex = mock(OrientVertex.class);
    final NestedAttributesMap attributes = mock(NestedAttributesMap.class);
    final NestedAttributesMap nugetAttributes = mock(NestedAttributesMap.class);

    when(tx.getAttributes(vertex)).thenReturn(attributes);
    when(attributes.child(eq(NAME))).thenReturn(nugetAttributes);

    when(vertex.getProperty(P_VERSION)).thenReturn(version);
    when(nugetAttributes.get(eq(P_VERSION), eq(String.class))).thenReturn(version);
    when(nugetAttributes.get(eq(P_IS_PRERELEASE), eq(Boolean.class))).thenReturn(isPrerelease);

    // placebo the other values
    when(nugetAttributes.get(anyString(), eq(Integer.class))).thenReturn(0);
    return vertex;
  }

  private void verifyVersionFlags(final NestedAttributesMap preReleaseAttributes, final boolean islatest,
                                  final boolean isAbsoluteLatest)
  {
    verify(preReleaseAttributes).set(eq(P_IS_LATEST_VERSION), eq(islatest));
    verify(preReleaseAttributes).set(eq(P_IS_ABSOLUTE_LATEST_VERSION), eq(isAbsoluteLatest));
  }

  @Test
  public void versionsOrderCorrectly() {

    final Comparator<String> comparator = new Comparator<String>()
    {
      private final VersionScheme SCHEME = new GenericVersionScheme();

      @Override
      public int compare(final String o1, final String o2) {
        try {
          final Version v1 = SCHEME.parseVersion(o1);
          final Version v2 = SCHEME.parseVersion(o2);
          return v1.compareTo(v2);
        }
        catch (InvalidVersionSpecificationException e) {
          throw Throwables.propagate(e);
        }
      }
    };

    final SortedSet<String> strings = Sets.newTreeSet(comparator);

    strings.add("2.1.8-SNAPSHOT");
    strings.add("1.0.0");
    strings.add("2.1.8-cannoli");
    strings.add("2.1.8-alpha");
    strings.add("2.1.8");
    strings.add("2.1.7");
    strings.add("2.1.7-garfunkel");
    strings.add("2.1.9");
    strings.add("2.1.10");
    strings.add("2.1.7-SNAPSHOT");

    System.err.println(strings);
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