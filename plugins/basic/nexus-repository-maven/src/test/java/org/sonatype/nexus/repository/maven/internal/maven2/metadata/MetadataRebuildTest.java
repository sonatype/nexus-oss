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
package org.sonatype.nexus.repository.maven.internal.maven2.metadata;

import java.io.IOException;
import java.util.Arrays;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.orient.HexRecordIdObfuscator;
import org.sonatype.nexus.orient.PersistentDatabaseInstanceRule;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.maven.internal.MavenFacet;
import org.sonatype.nexus.repository.maven.internal.MavenFacetImpl;
import org.sonatype.nexus.repository.maven.internal.MavenPathParser;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2MavenPathParser;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.repository.storage.AssetEntityAdapter;
import org.sonatype.nexus.repository.storage.BucketEntityAdapter;
import org.sonatype.nexus.repository.storage.ComponentEntityAdapter;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageFacetImpl;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.processors.ComponentGroupNames;
import org.sonatype.nexus.repository.storage.processors.ComponentGroups;
import org.sonatype.nexus.repository.storage.processors.Loop;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Stopwatch;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.inject.util.Providers;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * UT for toying.
 *
 * @since 3.0
 */
public class MetadataRebuildTest
    extends TestSupport
{
  @Rule
  public PersistentDatabaseInstanceRule database = new PersistentDatabaseInstanceRule("test");

  protected Repository mavenCentral = mock(Repository.class);

  protected Repository mavenSnapshots = mock(Repository.class);

  protected Maven2Format maven2Format = new Maven2Format();

  protected AssetEntityAdapter assetEntityAdapter;

  protected StorageFacetImpl storageFacet;

  protected MavenFacetImpl mavenFacet;

  @Before
  public void setUp() throws Exception {
    BlobStoreManager mockBlobStoreManager = mock(BlobStoreManager.class);
    when(mockBlobStoreManager.get(anyString())).thenReturn(mock(BlobStore.class));
    BucketEntityAdapter bucketEntityAdapter = new BucketEntityAdapter();
    HexRecordIdObfuscator recordIdObfuscator = new HexRecordIdObfuscator();
    bucketEntityAdapter.installDependencies(recordIdObfuscator);
    ComponentEntityAdapter componentEntityAdapter = new ComponentEntityAdapter(bucketEntityAdapter);
    componentEntityAdapter.installDependencies(recordIdObfuscator);
    assetEntityAdapter = new AssetEntityAdapter(bucketEntityAdapter, componentEntityAdapter);
    assetEntityAdapter.installDependencies(recordIdObfuscator);
    storageFacet = new StorageFacetImpl(
        mockBlobStoreManager,
        Providers.of(database.getInstance()),
        bucketEntityAdapter, componentEntityAdapter, assetEntityAdapter
    );
    storageFacet.installDependencies(mock(EventBus.class));

    mavenFacet = new MavenFacetImpl(mock(MimeSupport.class),
        ImmutableMap.<String, MavenPathParser>of(Maven2Format.NAME, new Maven2MavenPathParser()));
    mavenFacet.installDependencies(mock(EventBus.class));

    ConfigurationFacet configurationFacet = mock(ConfigurationFacet.class);
    when(configurationFacet.readSection(
        any(Configuration.class),
        eq("storage"),
        any(Class.class)))
        .thenReturn(new StorageFacetImpl.Config());
    when(configurationFacet.readSection(
        any(Configuration.class),
        eq("maven"),
        any(Class.class)))
        .thenReturn(new MavenFacetImpl.Config());

    when(mavenCentral.getName()).thenReturn("maven-central");
    when(mavenCentral.getFormat()).thenReturn(maven2Format);
    when(mavenCentral.facet(ConfigurationFacet.class)).thenReturn(configurationFacet);
    when(mavenCentral.facet(SearchFacet.class)).thenReturn(mock(SearchFacet.class));
    when(mavenCentral.facet(MavenFacet.class)).thenReturn(mavenFacet);
    when(mavenCentral.facet(StorageFacet.class)).thenReturn(storageFacet);

    when(mavenSnapshots.getName()).thenReturn("maven-snapshots");
    when(mavenSnapshots.getFormat()).thenReturn(maven2Format);
    when(mavenSnapshots.facet(ConfigurationFacet.class)).thenReturn(configurationFacet);
    when(mavenSnapshots.facet(SearchFacet.class)).thenReturn(mock(SearchFacet.class));

    storageFacet.attach(mavenCentral);
    storageFacet.init();
    storageFacet.start();

    mavenFacet.attach(mavenCentral);
    mavenFacet.init();
    mavenFacet.start();
  }

  @Before
  public void importDatabase() throws IOException {
    try (ODatabaseDocumentTx db = database.getInstance().acquire()) {
      final ODatabaseImport imp = new ODatabaseImport(db, util.resolvePath("src/test/resources/component.gz"),
          new OCommandOutputListener()
          {
            @Override
            public void onMessage(final String iText) {
              logger.info(iText);
            }
          });
      imp.importDatabase();
      imp.close();
    }
  }

  @After
  public void tearDown() throws Exception {
    mavenFacet.stop();
    storageFacet.stop();
  }

  public Stopwatch rebuildMetadataNested(final Repository repository) {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);

    final MetadataBuilder metadataBuilder = new MetadataBuilder();
    final MetadataUpdater metadataUpdater = new MetadataUpdater(mavenFacet);

    final ProcessorContext context = new ProcessorContext(
        new Supplier<StorageTx>()
        {
          @Override
          public StorageTx get() {
            return repository.facet(StorageFacet.class).openTx();
          }
        },
        Arrays.asList(
            // TODO: narrow loops to given repository
            new Loop<>(new ComponentGroups()), // -- loop distinct(group)
            new GProcessor(metadataBuilder, metadataUpdater), // around groups
            // TODO: narrow loops to given repository
            new Loop<>(new ComponentGroupNames()), // -- loop distinct(name)
            new AProcessor(metadataBuilder, metadataUpdater), // around names
            // TODO: narrow loops to given repository
            new Loop<>(new ComponentGroupNameBaseVersions()), // -- loop distinct(attributes.maven2.baseVersion)
            new BVProcessor(metadataBuilder, metadataUpdater), // around baseVersion
            new MetadataProcessor(metadataBuilder, mavenFacet.getMavenPathParser()) // comp+asset
        ).listIterator()
    );
    final Stopwatch stopwatch = Stopwatch.createStarted();
    context.proceed();
    stopwatch.stop();
    logger.info("t={}", stopwatch);
    return stopwatch;
  }

  public Stopwatch rebuildMetadataFlat(final Repository repository) {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);

    final MetadataBuilder metadataBuilder = new MetadataBuilder();
    final MetadataUpdater metadataUpdater = new MetadataUpdater(mavenFacet);

    final ProcessorContext context = new ProcessorContext(
        new Supplier<StorageTx>()
        {
          @Override
          public StorageTx get() {
            return repository.facet(StorageFacet.class).openTx();
          }
        },
        Arrays.asList(
            new Loop<>(new GAbVsElementSource()), // -- loop distinct(group, name, set(bV))
            new Loop<>(new GAbVElementSource()), // -- expand(bV)
            new GAbVExpando(), // GAbV -> P_XXX properties
            new GProcessor(metadataBuilder, metadataUpdater), // around groups
            new AProcessor(metadataBuilder, metadataUpdater), // around names
            new BVProcessor(metadataBuilder, metadataUpdater), // around baseVersion
            new MetadataProcessor(metadataBuilder, mavenFacet.getMavenPathParser()) // comp+asset
        ).listIterator()
    );
    final Stopwatch stopwatch = Stopwatch.createStarted();
    context.proceed();
    stopwatch.stop();
    logger.info("t={}", stopwatch);
    return stopwatch;
  }

  @Test
  public void process() throws Exception {
    final Stopwatch nested = rebuildMetadataNested(mavenCentral);
    final Stopwatch flat = rebuildMetadataFlat(mavenCentral);

    logger.info("tNested={}, tFlat={}", nested, flat);
  }
}
