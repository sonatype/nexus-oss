/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.plugins.p2.repository;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.plugins.p2.repository.proxy.P2ProxyRepository;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.security.SecuritySystem;

public class P2ProxyMirrorsTest
    extends AbstractNexusTestCase
{
    protected Nexus nexus;

    protected NexusConfiguration nexusConfiguration;

    private P2ProxyRepository repository;

    @Override
    protected void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        super.customizeContainerConfiguration( configuration );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        copyDefaultConfigToPlace();

        // next line will force initialization of P2 repository types
        lookup( TemplateProvider.class, "p2-repository" );

        nexus = lookup( Nexus.class );

        nexusConfiguration = lookup( NexusConfiguration.class );

        repository = (P2ProxyRepository) lookup( RepositoryRegistry.class ).getRepository( "p2-repo" );
        repository.setChecksumPolicy( ChecksumPolicy.IGNORE );
        final String remoteUrl = repository.getRemoteUrl();
        final MockRemoteStorage mockStorage = (MockRemoteStorage) this.lookup( RemoteRepositoryStorage.class, "mock" );
        repository.setRemoteUrl( remoteUrl );
        repository.setRemoteStorage( mockStorage );

        this.lookup( SecuritySystem.class ).setSecurityEnabled( false );
    }

    private void copyFileToDotNexus( final String fileName, final String targetFileName )
        throws IOException
    {
        final String localStorageDir = new URL( repository.getLocalUrl() ).getPath();

        final String filePrefix = this.getClass().getName().replaceAll( "\\.", "/" ) + "-";
        final String resource = filePrefix + fileName;

        final String destinationPath = P2Constants.PRIVATE_ROOT + "/" + targetFileName;
        final File destination = new File( localStorageDir, destinationPath );

        destination.getParentFile().mkdirs();
        FileUtils.copyFile( new File( "target/test-classes", resource ), destination );

    }

    @Override
    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        copyResource( "/nexus.xml", new File( getConfHomeDir(), "nexus.xml" ).getAbsolutePath() );
    }

    @Test
    @Ignore
    public void testVerifyBlackList()
        throws AccessDeniedException, IllegalOperationException, ItemNotFoundException, IOException
    {

        copyFileToDotNexus( "mirrors.xml", "mirrors.xml" );
        copyFileToDotNexus( "artifact-mappings.xml", "artifact-mappings.xml" );

        final MockRemoteStorage remoteStorage = (MockRemoteStorage) repository.getRemoteStorage();
        remoteStorage.getDownUrls().add( "http://remote3/" );
        remoteStorage.getValidUrls().add( "http://default/test/remote3/file1.txt" );
        remoteStorage.getValidUrls().add( "http://default/test/remote3/file2.txt" );

        // not found with bad mirror
        ResourceStoreRequest request = new ResourceStoreRequest( "/remote3/file1.txt" );

        repository.retrieveItem( request );

        // make sure we hit the mirror
        Assert.assertTrue( remoteStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, request, "http://remote3/" ) ) );
        Assert.assertTrue( remoteStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, request, "http://remote2/" ) ) );
        Assert.assertTrue( remoteStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, request, "http://remote1/" ) ) );
        // clear the requests
        remoteStorage.getRequests().clear();

        request = new ResourceStoreRequest( "/remote3/file2.txt" );

        repository.retrieveItem( request );

        // make sure we did NOT hit the mirror
        Assert.assertFalse( remoteStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, request, "http://remote3/" ) ) );
        Assert.assertTrue( remoteStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, request, "http://remote2/" ) ) );
        Assert.assertTrue( remoteStorage.getRequests().contains(
            new MockRemoteStorage.MockRequestRecord( repository, request, "http://remote1/" ) ) );
    }

    @Test
    @Ignore
    public void testVerifyBlackListWithCompositeRepo()
        throws AccessDeniedException, IllegalOperationException, ItemNotFoundException, IOException
    {
        // all we need is a different mirrors.xml
        copyFileToDotNexus( "mirrors-composite.xml", "mirrors.xml" );
        copyFileToDotNexus( "artifact-mappings-composite.xml", "artifact-mappings.xml" );

        testVerifyBlackList();
    }

    @Test
    @Ignore
    public void testCompositeRepoWithNoMirrors()
        throws AccessDeniedException, IllegalOperationException, ItemNotFoundException, IOException
    {
        copyFileToDotNexus( "mirrors-none-composite.xml", "mirrors.xml" );
        copyFileToDotNexus( "artifact-mappings-composite.xml", "artifact-mappings.xml" );

        final MockRemoteStorage remoteStorage = (MockRemoteStorage) repository.getRemoteStorage();
        // remoteStorage.getDownUrls().add( "http://remote3/" );
        remoteStorage.getValidUrls().add( "http://default/member2/remote2/file1.txt" );
        // remoteStorage.getValidUrls().add( "http://default/test/remote3/file2.txt" );

        // not found with bad mirror
        final ResourceStoreRequest request = new ResourceStoreRequest( "/remote2/file1.txt" );

        repository.retrieveItem( request );
    }
}
