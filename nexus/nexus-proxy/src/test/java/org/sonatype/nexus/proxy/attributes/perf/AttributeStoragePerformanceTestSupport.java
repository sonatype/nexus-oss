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
package org.sonatype.nexus.proxy.attributes.perf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
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
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.attributes.StorageItemInspector;
import org.sonatype.nexus.proxy.attributes.perf.internal.TMockRepository;
import org.sonatype.nexus.proxy.attributes.perf.internal.TestRepositoryItemUid;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DummyRepositoryItemUidFactory;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSPeer;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.google.common.collect.Maps;

/**
 * The performance tests for specific implementations of AttributesStorage.
 */
public abstract class AttributeStoragePerformanceTestSupport
{

    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    private Repository repository;

    private AttributeStorage attributeStorage;

    protected ApplicationConfiguration applicationConfiguration;

    final protected String testFilePath = "content/file.txt";

    private DefaultFSLocalRepositoryStorage localRepositoryStorageUnderTest;

    protected long originalLastAccessTime;

    final private DummyRepositoryItemUidFactory repositoryItemUidFactory = new DummyRepositoryItemUidFactory();

    final private File CONTENT_TEST_FILE = new File( "target/" + getClass().getSimpleName() + "/testContent.txt" );

    final private String SHA1_ATTRIBUTE_KEY = "digest.sha1";

    final private String SHA1_ATTRIBUTE_VALUE = "100f4ae295695335ef5d3346b42ed81ecd063fc9";

    final private static int ITERATION_COUNT = 100;

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
        CONTENT_TEST_FILE.getParentFile().mkdirs();
        org.codehaus.plexus.util.FileUtils.fileWrite( CONTENT_TEST_FILE, "CONTENT" );

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
        attributeStorage = getAttributeStorage();
        DefaultAttributesHandler attributesHandler =
            new DefaultAttributesHandler( applicationConfiguration, attributeStorage, null, itemInspectorList,
                fileItemInspectorList );
        // this test assumes attributes are ALWAYS written
        attributesHandler.setLastRequestedResolution( 0 );

        // Need to use the MockRepository
        // Do NOT us a mockito mock, using answers does not play well with junitbenchmark
        repository = new TMockRepository( "testRetieveItem-repo", new DummyRepositoryItemUidFactory() );
        repository.setLocalUrl( repoLocalURL );
        repository.setAttributesHandler( attributesHandler );

        // setup the class under test
        localRepositoryStorageUnderTest =
            new DefaultFSLocalRepositoryStorage( wastebasket, linkPersister, mimeSupport, repositoryContexts,
                new DefaultFSPeer() );

        // prime the retrieve
        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        originalLastAccessTime =
            localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest ).getLastRequested();
        Thread.sleep( 10 );
    }

    public abstract AttributeStorage getAttributeStorage();

    // ////////////
    // Test writes
    // ////////////
    @Test
    public void test0PrimePutAndGettAttribute()
        throws IOException
    {
        writeEntryToAttributeStorage( "/prime.txt" );
        getStorageItemFromAttributeStore( "/prime.txt" );
    }

    @Test
    public void test1PutAttribute()
        throws IOException
    {
        writeEntryToAttributeStorage( "/a.txt" );
    }

    @Test
    public void test2PutAttributeX100()
        throws IOException
    {
        for ( int ii = 0; ii < ITERATION_COUNT; ii++ )
        {
            writeEntryToAttributeStorage( "/" + ii + ".txt" );
        }
    }

    @Test
    public void test3GetAttribute()
        throws IOException
    {
        Attributes storageItem = getStorageItemFromAttributeStore( "/a.txt" );
        assertThat( storageItem.get( SHA1_ATTRIBUTE_KEY ), equalTo( SHA1_ATTRIBUTE_VALUE ) );
    }

    @Test
    public void test4GetAttributeX100()
        throws IOException
    {
        for ( int ii = 0; ii < ITERATION_COUNT; ii++ )
        {
            getStorageItemFromAttributeStore( "/" + ii + ".txt" );
        }
    }

    // @Test
    public void test5DeleteAttributes()
        throws IOException
    {
        deleteStorageItemFromAttributeStore( "/a.txt" );
    }

    // @Test
    public void test6DeleteAttributesX100()
        throws IOException
    {
        for ( int ii = 0; ii < ITERATION_COUNT; ii++ )
        {
            deleteStorageItemFromAttributeStore( "/" + ii + ".txt" );
        }
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

    protected void writeEntryToAttributeStorage( String path )
        throws IOException
    {
        StorageFileItem storageFileItem =
            new DefaultStorageFileItem( repository, new ResourceStoreRequest( path ), true, true, getContentLocator() );

        storageFileItem.getRepositoryItemAttributes().put( SHA1_ATTRIBUTE_KEY, SHA1_ATTRIBUTE_VALUE );
        storageFileItem.getRepositoryItemAttributes().put( "digest.md5", "f62472816fb17de974a87513e2257d63" );
        storageFileItem.getRepositoryItemAttributes().put( "request.address", "127.0.0.1" );

        attributeStorage.putAttributes( storageFileItem.getRepositoryItemUid(),
            storageFileItem.getRepositoryItemAttributes() );
    }

    protected Attributes getStorageItemFromAttributeStore( String path )
        throws IOException
    {
        RepositoryItemUid repositoryItemUid = new TestRepositoryItemUid( repositoryItemUidFactory, repository, path );

        return attributeStorage.getAttributes( repositoryItemUid );
    }

    private void deleteStorageItemFromAttributeStore( String path )
        throws IOException
    {
        RepositoryItemUid repositoryItemUid = new TestRepositoryItemUid( repositoryItemUidFactory, repository, path );
        assertThat( "Attribute was not removed from store.", attributeStorage.deleteAttributes( repositoryItemUid ) );
    }

    private ContentLocator getContentLocator()
    {
        if ( true )
        {
            return new StringContentLocator( "CONTENT" );
        }
        else
        {
            return new FileContentLocator( CONTENT_TEST_FILE, "text/plain" );
        }
    }

}
