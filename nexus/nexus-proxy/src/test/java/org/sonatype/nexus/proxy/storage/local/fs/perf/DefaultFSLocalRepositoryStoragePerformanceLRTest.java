/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.local.fs.perf;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.attributes.DefaultFSAttributeStorage;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.attributes.StorageItemInspector;
import org.sonatype.nexus.proxy.attributes.XStreamMarshaller;
import org.sonatype.nexus.proxy.attributes.perf.internal.TMockRepository;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DummyRepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSPeer;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.google.common.collect.Maps;

/**
 *
 */
@BenchmarkHistoryChart( )
@BenchmarkMethodChart( )
@AxisRange( min = 0 )
public class DefaultFSLocalRepositoryStoragePerformanceLRTest
{
    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    public ApplicationConfiguration applicationConfiguration;

    final private String testFilePath = "content/file.txt";

    private DefaultFSLocalRepositoryStorage localRepositoryStorageUnderTest;

    private Repository repository;

    private long originalLastAccessTime;

    @Before
    public void setup()
        throws Exception
    {
        File repoStorageDir = new File( "target/" + getClass().getSimpleName() + "/repo-storage/" );
        String repoLocalURL = repoStorageDir.getAbsoluteFile().toURI().toString();

        // write a test file
        File testFile = new File( repoStorageDir, testFilePath );
        testFile.getParentFile().mkdirs();
        org.codehaus.plexus.util.FileUtils.fileWrite( testFile, "CONTENT" );

        // Mocks
        Wastebasket wastebasket = mock( Wastebasket.class );
        LinkPersister linkPersister = mock( LinkPersister.class );
        MimeSupport mimeSupport = mock( MimeSupport.class );
        Map<String, Long> repositoryContexts = Maps.newHashMap();

        // Application Config
        applicationConfiguration = mock( ApplicationConfiguration.class );
        when( applicationConfiguration.getWorkingDirectory( eq( "proxy/attributes-ng" ) ) ).thenReturn(
            new File( "target/" + this.getClass().getSimpleName() + "/attributes-ng" ) );
        when( applicationConfiguration.getWorkingDirectory( eq( "proxy/attributes" ) ) ).thenReturn(
            new File( "target/" + this.getClass().getSimpleName() + "/attributes" ) );

        // remove any event inspectors from the timing
        List<StorageItemInspector> itemInspectorList = new ArrayList<StorageItemInspector>();
        List<StorageFileItemInspector> fileItemInspectorList = new ArrayList<StorageFileItemInspector>();

        // set the AttributeStorage on the Attribute Handler
        DefaultAttributesHandler attributesHandler =
            new DefaultAttributesHandler( applicationConfiguration, getAttributeStorage(), null, itemInspectorList,
                fileItemInspectorList );

        // Need to use the MockRepository
        repository = new TMockRepository( "testRetieveItem-repo", new DummyRepositoryItemUidFactory() );
        repository.setLocalUrl( repoLocalURL );
        repository.setAttributesHandler( attributesHandler );

        // setup the class under test
        localRepositoryStorageUnderTest =
            new DefaultFSLocalRepositoryStorage( wastebasket, linkPersister, mimeSupport, repositoryContexts,
                new DefaultFSPeer() );

        // this test expects "old" behaviour:
        ( (DefaultAttributesHandler) repository.getAttributesHandler() ).setLastRequestedResolution( 0 );

        // prime the retrieve
        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        originalLastAccessTime = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest ).getLastRequested();

        // sleep so we are sure the clock is different when we validate the last update time.
        Thread.sleep( 2 );
    }

    private AttributeStorage getAttributeStorage()
    {
        // Mock out the events
        DefaultFSAttributeStorage attributeStorage =
            new DefaultFSAttributeStorage( applicationConfiguration,
                new XStreamMarshaller() );
        attributeStorage.initializeWorkingDirectory();

        return attributeStorage;
    }

    @BenchmarkOptions( benchmarkRounds = 1000, warmupRounds = 1 )
    @Test
    public void testRetieveItemWithLastAccessUpdate()
        throws LocalStorageException, ItemNotFoundException
    {
        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        resourceRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.greaterThan( originalLastAccessTime ) );
    }

    @BenchmarkOptions( benchmarkRounds = 1000, warmupRounds = 1 )
    @Test
    public void testRetieveItemWithoutLastAccessUpdate()
        throws LocalStorageException, ItemNotFoundException
    {
        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.equalTo( originalLastAccessTime ) );
    }
}
