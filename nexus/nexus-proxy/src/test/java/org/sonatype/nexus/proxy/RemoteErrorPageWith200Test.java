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
package org.sonatype.nexus.proxy;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.commonshttpclient.CommonsHttpClientRemoteStorage;

public class RemoteErrorPageWith200Test
    extends AbstractProxyTestEnvironment
{

    private RemoteRepositoryStorage remoteStorage;

    private ProxyRepository aProxyRepository;

    private String baseUrl;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        this.remoteStorage =
            this.lookup( RemoteRepositoryStorage.class, CommonsHttpClientRemoteStorage.PROVIDER_STRING );
        aProxyRepository =
            lookup( RepositoryRegistry.class ).getRepositoryWithFacet( "200ErrorTest", ProxyRepository.class );
    }

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {

        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.baseUrl = ss.getUrl( "200ErrorTest" );
        return new M2TestsuiteEnvironmentBuilder( ss );

    }

    public void testRemoteReturnsErrorWith200StatusHeadersNotSet()
        throws ItemNotFoundException, IOException
    {

        String expectedContent = "my cool expected content";
        ErrorServlet.CONTENT = expectedContent;

        // remote request
        ResourceStoreRequest storeRequest = new ResourceStoreRequest( "random/file.txt" );
        DefaultStorageFileItem item =
            (DefaultStorageFileItem) remoteStorage.retrieveItem( aProxyRepository, storeRequest, this.baseUrl );

        // result should be HTML
        InputStream itemInputStrem = item.getInputStream();

        try
        {
            String content = IOUtil.toString( itemInputStrem );
            Assert.assertEquals( expectedContent, content );
        }
        finally
        {
            IOUtil.close( itemInputStrem );
        }
    }

    public void testRemoteReturnsErrorWith200StatusHeadersSet() throws RemoteAccessException, StorageException, ItemNotFoundException
    {

        String expectedContent = "error page";
        ErrorServlet.CONTENT = expectedContent;
        ErrorServlet.addHeader( CommonsHttpClientRemoteStorage.NEXUS_MISSING_ARTIFACT_HEADER, "true" );

        // remote request
        ResourceStoreRequest storeRequest = new ResourceStoreRequest( "random/file.txt" );
        try
        {
            DefaultStorageFileItem item =
                (DefaultStorageFileItem) remoteStorage.retrieveItem( aProxyRepository, storeRequest, this.baseUrl );
            Assert.fail( "expected  RemoteStorageException" );
        }
        // expect artifact not found
        catch ( RemoteStorageException e )
        {
            // expected
        }
    }

}
