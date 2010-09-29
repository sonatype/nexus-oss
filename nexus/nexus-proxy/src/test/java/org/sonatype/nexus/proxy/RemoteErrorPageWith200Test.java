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
