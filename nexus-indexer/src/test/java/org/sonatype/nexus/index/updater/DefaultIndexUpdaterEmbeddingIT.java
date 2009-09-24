package org.sonatype.nexus.index.updater;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferEvent;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.updater.fixtures.ServerTestFixture;
import org.sonatype.nexus.index.updater.fixtures.TransferListenerFixture;
import org.sonatype.nexus.index.updater.jetty.JettyResourceFetcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import junit.framework.TestCase;

public class DefaultIndexUpdaterEmbeddingIT
    extends TestCase
{
    private String baseUrl;

    private PlexusContainer container;

    private ServerTestFixture server;

    private IndexUpdater updater;

    public void testBasicIndexRetrieval()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setTransferListener( new TransferListenerFixture() );

            updater.fetchAndUpdateIndex( updateRequest );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    public void testBasicAuthenticatedIndexRetrieval()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl + "protected/" );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setAuthenticationInfo( new AuthenticationInfo()
            {
                private static final long serialVersionUID = 1L;

                {
                    setUserName( "user" );
                    setPassword( "password" );
                }
            } );
            updateRequest.setTransferListener( new TransferListenerFixture() );

            updater.fetchAndUpdateIndex( updateRequest );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    public void testAuthenticatedIndexRetrieval_LongAuthorizationHeader()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl + "protected/" );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setAuthenticationInfo( new AuthenticationInfo()
            {
                private static final long serialVersionUID = 1L;

                {
                    setUserName( "longuser" );
                    setPassword( ServerTestFixture.LONG_PASSWORD );
                }
            } );
            updateRequest.setTransferListener( new TransferListenerFixture() );

            updater.fetchAndUpdateIndex( updateRequest );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    public void testBasicHighLatencyIndexRetrieval()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl + "slow/" );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setAuthenticationInfo( new AuthenticationInfo()
            {
                private static final long serialVersionUID = 1L;

                {
                    setUserName( "user" );
                    setPassword( "password" );
                }
            } );
            updateRequest.setTransferListener( new TransferListenerFixture() );

            updater.fetchAndUpdateIndex( updateRequest );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    public void testHighLatencyIndexRetrieval_LowConnectionTimeout()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl + "slow/" );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setAuthenticationInfo( new AuthenticationInfo()
            {
                private static final long serialVersionUID = 1L;

                {
                    setUserName( "user" );
                    setPassword( "password" );
                }
            } );
            updateRequest.setTransferListener( new TransferListenerFixture()
            {
                @Override
                public void transferError( final TransferEvent transferEvent )
                {
                }
            } );

            ResourceFetcher fetcher =
                new JettyResourceFetcher().setConnectionTimeoutMillis( 100 )
                                          .setAuthenticationInfo( updateRequest.getAuthenticationInfo() )
                                          .addTransferListener( updateRequest.getTransferListener() );

            updateRequest.setResourceFetcher( fetcher );

            try
            {
                updater.fetchAndUpdateIndex( updateRequest );
                fail( "Should timeout and throw IOException." );
            }
            catch ( IOException e )
            {
                System.out.println( "Operation timed out due to short connection timeout, as expected." );
            }
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    public void testHighLatencyIndexRetrieval_LowTransactionTimeout()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl + "slow/" );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setAuthenticationInfo( new AuthenticationInfo()
            {
                private static final long serialVersionUID = 1L;

                {
                    setUserName( "user" );
                    setPassword( "password" );
                }
            } );
            updateRequest.setTransferListener( new TransferListenerFixture()
            {
                @Override
                public void transferError( final TransferEvent transferEvent )
                {
                }
            } );

            ResourceFetcher fetcher =
                new JettyResourceFetcher().setTransactionTimeoutMillis( 100 )
                                          .setAuthenticationInfo( updateRequest.getAuthenticationInfo() )
                                          .addTransferListener( updateRequest.getTransferListener() );

            updateRequest.setResourceFetcher( fetcher );

            try
            {
                updater.fetchAndUpdateIndex( updateRequest );
                fail( "Should timeout and throw IOException." );
            }
            catch ( IOException e )
            {
                System.out.println( "Operation timed out due to short transaction timeout, as expected." );
            }
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    public void testIndexRetrieval_InfiniteRedirection()
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        File basedir = File.createTempFile( "nexus-indexer.", ".dir" );
        basedir.delete();
        basedir.mkdirs();

        try
        {
            IndexingContext ctx = newTestContext( basedir, baseUrl + "redirect-trap/" );

            IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
            updateRequest.setTransferListener( new TransferListenerFixture()
            {
                @Override
                public void transferError( final TransferEvent transferEvent )
                {
                }
            } );

            ResourceFetcher fetcher =
                new JettyResourceFetcher().addTransferListener( updateRequest.getTransferListener() );

            updateRequest.setResourceFetcher( fetcher );

            try
            {
                updater.fetchAndUpdateIndex( updateRequest );
                fail( "Should throw IOException from too many redirects." );
            }
            catch ( IOException e )
            {
                System.out.println( "Operation timed out due to too many redirects, as expected." );
            }
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( basedir );
            }
            catch ( IOException e )
            {
            }
        }
    }

    private IndexingContext newTestContext( final File basedir, final String baseUrl )
        throws IOException, UnsupportedExistingLuceneIndexException, ComponentLookupException
    {
        IndexCreator min = container.lookup( IndexCreator.class, "min" );
        IndexCreator jar = container.lookup( IndexCreator.class, "jarContent" );

        List<IndexCreator> creators = new ArrayList<IndexCreator>();
        creators.add( min );
        creators.add( jar );

        String repositoryId = "test";

        return new DefaultIndexingContext( repositoryId, repositoryId, basedir, basedir, baseUrl, baseUrl, creators,
                                           true );
    }

    @Override
    public void setUp()
        throws Exception
    {
        // FIXME: Try to detect the port from the system environment.
        int port = -1;
        try
        {
            ResourceBundle bundle = ResourceBundle.getBundle( "baseTest" );
            String portStr = bundle.getString( "index-server" );
            if ( portStr != null )
            {
                port = Integer.parseInt( portStr );
            }
        }
        catch ( MissingResourceException e )
        {
        }

        if ( port < 1024 )
        {
            System.out.println( "Using default port: 8080" );
            port = 8080;
        }

        baseUrl = "http://127.0.0.1:" + port + "/";

        server = new ServerTestFixture( port );
        container = new DefaultPlexusContainer();

        updater = container.lookup( IndexUpdater.class, "default" );
    }

    @Override
    public void tearDown()
        throws Exception
    {
        container.release( updater );
        container.dispose();
        server.stop();
    }
}
