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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;

import com.sonatype.nexus.repository.nuget.odata.ODataFeedUtils;

import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.common.time.Clock;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.id.ORID;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.eclipse.aether.version.VersionScheme;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NugetGalleryFacetImplPutTest
    extends TestSupport
{
  @Mock
  private EventBus eventBus;

  @Mock
  private Repository repository;

  @Mock
  private SearchFacet searchFacet;

  @Before
  public void wireSearchFacet() {
    when(repository.facet(SearchFacet.class)).thenReturn(searchFacet);
  }

  @Test
  public void putCreatesPackageMetadataAndBlob() throws Exception {
    putPackageMetadataAndBlob(true);
  }

  @Test
  public void putUpdatesPackageMetadataAndBlob() throws Exception {
    putPackageMetadataAndBlob(false);
  }

  @SuppressWarnings("unchecked")
  private void putPackageMetadataAndBlob(final boolean isNew) throws Exception
  {
    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);

    final InputStream packageStream = getClass().getResourceAsStream("/SONATYPE.TEST.1.0.nupkg");

    doNothing().when(galleryFacet).maintainAggregateInfo(eq("SONATYPE.TEST"));

    Component component = mock(Component.class);
    ORID orid = mock(ORID.class);
    doReturn(component).when(galleryFacet).createOrUpdatePackageAndContents(any(Map.class), any(InputStream.class));
    when(component.isNew()).thenReturn(isNew);

    galleryFacet.put(packageStream);

    verify(galleryFacet).maintainAggregateInfo("SONATYPE.TEST");
  }

  @Test
  public void derivedAttributesSetForNewComponents() {
    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);
    final Clock clock = new TestableClock();
    galleryFacet.clock = clock;

    final Map<String, String> incomingMap = Maps.newHashMap();
    final Map<String, Object> storedMap = Maps.newHashMap();
    galleryFacet.setDerivedAttributes(incomingMap, new NestedAttributesMap("NUGET", storedMap), false);

    assertThat(((Date) storedMap.get(P_CREATED)).getTime(), is(clock.millis()));
    assertThat(((Date) storedMap.get(P_PUBLISHED)).getTime(), is(clock.millis()));
    assertThat((int) storedMap.get(P_DOWNLOAD_COUNT), is(0));
    assertThat((int) storedMap.get(P_VERSION_DOWNLOAD_COUNT), is(0));
  }

  @Test
  public void derivedAttributesSetForHostedRepublishedComponents() {
    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);
    final Clock clock = new TestableClock();
    galleryFacet.clock = clock;

    final Map<String, String> incomingMap = Maps.newHashMap();
    incomingMap.put(DOWNLOAD_COUNT, "20");
    incomingMap.put(VERSION_DOWNLOAD_COUNT, "12");
    final String feedDate = ODataFeedUtils.datetime(clock.millis());
    incomingMap.put(CREATED, feedDate);
    incomingMap.put(PUBLISHED, feedDate);

    final NestedAttributesMap storedAttributes = mock(NestedAttributesMap.class);
    galleryFacet.setDerivedAttributes(incomingMap, storedAttributes, true);

    verify(storedAttributes).set(eq(P_CREATED), eq(clock.dateTime().toDate()));
    verify(storedAttributes).set(eq(P_PUBLISHED), eq(clock.dateTime().toDate()));
  }

  @Test
  public void derivedAttributesSetForProxyRepublishedComponents() {
    final NugetGalleryFacetImpl galleryFacet = buildSpy(false);
    final Clock clock = new TestableClock();
    galleryFacet.clock = clock;

    final Map<String, String> incomingMap = Maps.newHashMap();
    incomingMap.put(DOWNLOAD_COUNT, "20");
    incomingMap.put(VERSION_DOWNLOAD_COUNT, "12");
    final String feedDate = ODataFeedUtils.datetime(clock.millis());
    incomingMap.put(CREATED, feedDate);
    incomingMap.put(PUBLISHED, feedDate);

    final NestedAttributesMap storedAttributes = mock(NestedAttributesMap.class);
    galleryFacet.setDerivedAttributes(incomingMap, storedAttributes, true);

    verify(storedAttributes).set(eq(P_CREATED), eq(clock.dateTime().toDate()));
    verify(storedAttributes).set(eq(P_PUBLISHED), eq(clock.dateTime().toDate()));
  }

  @Test
  public void derivedAttributesSetForProxyNewComponents() {
    final NugetGalleryFacetImpl galleryFacet = buildSpy(false);
    final Clock clock = new TestableClock();
    galleryFacet.clock = clock;

    final Map<String, String> incomingMap = Maps.newHashMap();
    incomingMap.put(DOWNLOAD_COUNT, "20");
    incomingMap.put(VERSION_DOWNLOAD_COUNT, "12");
    final String feedDate = ODataFeedUtils.datetime(clock.millis());
    incomingMap.put(CREATED, feedDate);
    incomingMap.put(PUBLISHED, feedDate);

    final NestedAttributesMap storedAttributes = mock(NestedAttributesMap.class);
    galleryFacet.setDerivedAttributes(incomingMap, storedAttributes, false);

    verify(storedAttributes).set(eq(P_CREATED), eq(clock.dateTime().toDate()));
    verify(storedAttributes).set(eq(P_PUBLISHED), eq(clock.dateTime().toDate()));
    verify(storedAttributes).set(eq(P_DOWNLOAD_COUNT), eq(20));
    verify(storedAttributes).set(eq(P_VERSION_DOWNLOAD_COUNT), eq(12));
  }

  // single pre-release
  // single release

  // mix, latest is pre-release
  // mix, latest is release

  @Test
  public void versionsFlagForSinglePrerelease() {
    final StorageTx tx = mock(StorageTx.class);

    final Component preRelease = buildVersionMock(tx, "2.1.8-greenbell", true);

    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);


    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(preRelease));

    verifyVersionFlags(tx.firstAsset(preRelease).formatAttributes(), false, true);
  }

  @Test
  public void versionsFlagForSingleRelease() {
    final StorageTx tx = mock(StorageTx.class);

    final Component release = buildVersionMock(tx, "2.1.8", false);

    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(release));

    verifyVersionFlags(tx.firstAsset(release).formatAttributes(), true, true);
  }

  @Test
  public void versionsFlagForReleaseIsLatest() {
    final StorageTx tx = mock(StorageTx.class);

    final Component release = buildVersionMock(tx, "2.1.8", false);
    // TODO: Aether doesn't correctly order 2.1.7-greenbell and 2.1.7
    final Component preRelease = buildVersionMock(tx, "2.1.7-greenbell", true);

    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(release, preRelease));

    verifyVersionFlags(tx.firstAsset(release).formatAttributes(), true, true);
    verifyVersionFlags(tx.firstAsset(preRelease).formatAttributes(), false, false);
  }

  @Test
  public void versionsFlagForPrereleaseIsLatest() {
    final StorageTx tx = mock(StorageTx.class);

    final Component preRelease = buildVersionMock(tx, "2.1.9-greenbell", true);
    final Component release = buildVersionMock(tx, "2.1.8", false);

    final NugetGalleryFacetImpl galleryFacet = buildSpy(true);
    galleryFacet.maintainAggregateInfo(tx, Arrays.asList(release, preRelease));

    verifyVersionFlags(tx.firstAsset(preRelease).formatAttributes(), false, true);
    verifyVersionFlags(tx.firstAsset(release).formatAttributes(), true, false);
  }

  private NugetGalleryFacetImpl buildSpy(final boolean hosted) {
    final NugetGalleryFacetImpl galleryFacet = Mockito.spy(new NugetGalleryFacetImpl()
    {
      @Override
      protected Repository getRepository() {
        return repository;
      }
    });
    galleryFacet.installDependencies(eventBus);
    doReturn(hosted).when(galleryFacet).isRepoAuthoritative();
    return galleryFacet;
  }

  private Component buildVersionMock(final StorageTx tx, final String version, final boolean isPrerelease) {
    final Component component = mock(Component.class);
    final Asset asset = mock(Asset.class);
    final NestedAttributesMap nugetAttributes = mock(NestedAttributesMap.class);

    when(tx.firstAsset(component)).thenReturn(asset);
    when(asset.formatAttributes()).thenReturn(nugetAttributes);

    when(component.requireVersion()).thenReturn(version);
    when(nugetAttributes.require(eq(P_VERSION))).thenReturn(version);
    when(nugetAttributes.require(eq(P_IS_PRERELEASE), eq(Boolean.class))).thenReturn(isPrerelease);
    when(nugetAttributes.require(eq(P_VERSION_DOWNLOAD_COUNT), eq(Integer.class))).thenReturn(0);

    // placebo the other values
    when(nugetAttributes.get(anyString(), eq(Integer.class))).thenReturn(0);
    return component;
  }

  private void verifyVersionFlags(final NestedAttributesMap preReleaseAttributes, final boolean islatest,
                                  final boolean isAbsoluteLatest)
  {
    verify(preReleaseAttributes).set(eq(P_IS_LATEST_VERSION), eq(islatest));
    verify(preReleaseAttributes).set(eq(P_IS_ABSOLUTE_LATEST_VERSION), eq(isAbsoluteLatest));
  }

  /**
   * NOTE: Aether doesn't seem to respect the idea that 2.1.7-garfunkel should be before 2.1.7, so nupkg version
   * ordering
   * may be wonky.
   */
  @Test
  @Ignore
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
