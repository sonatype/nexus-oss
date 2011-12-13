/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.storage.local.fs.perf;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Mockito;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestEnvironment;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.attributes.AttributeStorage;
import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.attributes.DefaultAttributesHandler;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 *
 */
@BenchmarkHistoryChart( )
@BenchmarkMethodChart( )
@AxisRange( min = 0 )
public class DefaultFSLocalRepositoryStoragePerformanceITLRTest
    extends AbstractNexusTestEnvironment
{
    @Rule
    public MethodRule benchmarkRun = new BenchmarkRule();

    final private String testFilePath = "content/file.txt";

    private LocalRepositoryStorage localRepositoryStorageUnderTest;

    private Repository repository;

    private long originalLastAccessTime;

    @Before
    public void setup()
        throws Exception
    {
        ApplicationConfiguration applicationConfiguration = this.lookup( ApplicationConfiguration.class );

        File repositoryStorageDir = applicationConfiguration.getWorkingDirectory( "proxy/store/test-repo" );

        // adding one hosted
        repository = this.lookup( Repository.class, "maven2" );

        CRepository repoConf = new DefaultCRepository();

        repoConf.setProviderRole( Repository.class.getName() );
        repoConf.setProviderHint( "maven2" );
        repoConf.setId( "test-repo" );

        repoConf.setLocalStorage( new CLocalStorage() );
        repoConf.getLocalStorage().setProvider( "file" );
        repoConf.getLocalStorage().setUrl( repositoryStorageDir.toURI().toURL().toString() );

        Xpp3Dom exRepo = new Xpp3Dom( "externalConfiguration" );
        repoConf.setExternalConfiguration( exRepo );
        M2RepositoryConfiguration exRepoConf = new M2RepositoryConfiguration( exRepo );
        exRepoConf.setRepositoryPolicy( RepositoryPolicy.RELEASE );
        exRepoConf.setChecksumPolicy( ChecksumPolicy.STRICT_IF_EXISTS );

        repository.configure( repoConf );

        applicationConfiguration.getConfigurationModel().addRepository( repoConf );

        this.lookup( RepositoryRegistry.class ).addRepository( repository );

        localRepositoryStorageUnderTest = repository.getLocalStorage();

        // write a test file
        File testFile = new File( repositoryStorageDir, testFilePath );
        testFile.getParentFile().mkdirs();
        org.codehaus.plexus.util.FileUtils.fileWrite( testFile, "CONTENT" );

        // this test expects "old" behaviour:
        ( (DefaultAttributesHandler) repository.getAttributesHandler() ).setLastRequestedResolution( 0 );

        // prime the retrieve
        originalLastAccessTime = primeLastRequestedTimestamp();

        // sleep so we are sure the clock is different when we validate the last update time.
        Thread.sleep( 2 );
    }

    protected long primeLastRequestedTimestamp()
        throws LocalStorageException, ItemNotFoundException
    {
        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        resourceRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        return localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest ).getLastRequested();
    }

    @BenchmarkOptions( benchmarkRounds = 10, warmupRounds = 1 )
    @Test
    public void testRetieveItemWithoutLastAccessUpdate()
        throws LocalStorageException, ItemNotFoundException
    {
        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.equalTo( originalLastAccessTime ) );
    }

    @BenchmarkOptions( benchmarkRounds = 10, warmupRounds = 1 )
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

    @BenchmarkOptions( benchmarkRounds = 10, warmupRounds = 1 )
    @Test
    public void validateRetieveItemWithoutLastAccessUpdate()
        throws LocalStorageException, ItemNotFoundException, IOException
    {
        AttributeStorage attributeStorageSpy = Mockito.spy( repository.getAttributesHandler().getAttributeStorage() );
        repository.getAttributesHandler().setAttributeStorage( attributeStorageSpy );

        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.equalTo( originalLastAccessTime ) );

        Mockito.verify( attributeStorageSpy, Mockito.times( 1 ) ).getAttributes( Mockito.<RepositoryItemUid> any() );
    }

    @BenchmarkOptions( benchmarkRounds = 10, warmupRounds = 1 )
    @Test
    public void validateRetieveItemWithLastAccessUpdate()
        throws LocalStorageException, ItemNotFoundException, IOException
    {
        AttributeStorage attributeStorageSpy = Mockito.spy( repository.getAttributesHandler().getAttributeStorage() );
        repository.getAttributesHandler().setAttributeStorage( attributeStorageSpy );

        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        resourceRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.greaterThan( originalLastAccessTime ) );

        Mockito.verify( attributeStorageSpy, Mockito.times( 1 ) ).putAttributes( Mockito.<RepositoryItemUid> any(),
            Mockito.<Attributes> any() );
        Mockito.verify( attributeStorageSpy, Mockito.times( 1 ) ).getAttributes( Mockito.<RepositoryItemUid> any() );

    }

    @BenchmarkOptions( benchmarkRounds = 10, warmupRounds = 1 )
    @Test
    public void validateRetieveItemWithOutLastAccessUpdateTimeDelay()
        throws LocalStorageException, ItemNotFoundException, IOException
    {
        // do the test, but make sure the _iterations_ will not get out of the "resolution" span!
        // Note: this test is just broken as is, what guarantees it will finish the cycles in given X millis?
        // Meaning, the assertions + rounds is wrong.
        ( (DefaultAttributesHandler) repository.getAttributesHandler() ).setLastRequestedResolution( 30000L );
        AttributeStorage attributeStorageSpy = Mockito.spy( repository.getAttributesHandler().getAttributeStorage() );
        repository.getAttributesHandler().setAttributeStorage( attributeStorageSpy );

        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        resourceRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.equalTo( originalLastAccessTime ) );

        Mockito.verify( attributeStorageSpy, Mockito.times( 0 ) ).putAttributes( Mockito.<RepositoryItemUid> any(),
            Mockito.<Attributes> any() );
        Mockito.verify( attributeStorageSpy, Mockito.times( 1 ) ).getAttributes( Mockito.<RepositoryItemUid> any() );

    }

    @BenchmarkOptions( benchmarkRounds = 1, warmupRounds = 0 )
    @Test
    public void validateRetieveItemWithLastAccessUpdateTimeDelay()
        throws Exception
    {
        // need to ensure we wait 100 ms
        int wait = 100;

        ( (DefaultAttributesHandler) repository.getAttributesHandler() ).setLastRequestedResolution( wait );
        Thread.sleep( wait );

        AttributeStorage attributeStorageSpy = Mockito.spy( repository.getAttributesHandler().getAttributeStorage() );
        repository.getAttributesHandler().setAttributeStorage( attributeStorageSpy );

        ResourceStoreRequest resourceRequest = new ResourceStoreRequest( testFilePath );
        resourceRequest.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

        AbstractStorageItem storageItem = localRepositoryStorageUnderTest.retrieveItem( repository, resourceRequest );

        MatcherAssert.assertThat( storageItem, Matchers.notNullValue() );
        MatcherAssert.assertThat( storageItem.getLastRequested(), Matchers.greaterThan( originalLastAccessTime ) );

        Mockito.verify( attributeStorageSpy, Mockito.times( 1 ) ).putAttributes( Mockito.<RepositoryItemUid> any(),
            Mockito.<Attributes> any() );
        Mockito.verify( attributeStorageSpy, Mockito.times( 1 ) ).getAttributes( Mockito.<RepositoryItemUid> any() );

    }

}
