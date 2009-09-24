package org.sonatype.nexus.index.updater;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.index.context.DefaultIndexingContext;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FullBootProofOfConcept
{

    public static void main( final String[] args )
        throws IOException
    {
        for ( int i = 0; i < 2; i++ )
        {
            File basedir = File.createTempFile( "nexus-indexer.", ".dir" );

            try
            {
                run( basedir );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            catch ( ComponentLookupException e )
            {
                e.printStackTrace();
            }
            catch ( PlexusContainerException e )
            {
                e.printStackTrace();
            }
            catch ( ParseException e )
            {
                e.printStackTrace();
            }
            catch ( UnsupportedExistingLuceneIndexException e )
            {
                e.printStackTrace();
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
    }

    public static void run( final File basedir )
        throws IOException, ComponentLookupException, PlexusContainerException, ParseException,
        UnsupportedExistingLuceneIndexException
    {
        try
        {
            FileUtils.forceDelete( basedir );
        }
        catch ( IOException e )
        {
            // just do your best to delete this.
        }

        basedir.mkdirs();

        PlexusContainer container = new DefaultPlexusContainer();

        IndexCreator min = container.lookup( IndexCreator.class, "min" );
        IndexCreator jar = container.lookup( IndexCreator.class, "jarContent" );

        List<IndexCreator> creators = new ArrayList<IndexCreator>();
        creators.add( min );
        creators.add( jar );

        String repositoryId = "test";
        String repositoryUrl = "http://repository.sonatype.org/content/groups/sonatype/";
        // String repositoryUrl = "http://repo1.maven.org/maven2/";
        String indexUrl = repositoryUrl + ".index";

        IndexingContext ctx =
            new DefaultIndexingContext( repositoryId, repositoryId, basedir, basedir, repositoryUrl, indexUrl,
                                        creators, true );

        IndexUpdateRequest updateRequest = new IndexUpdateRequest( ctx );
        updateRequest.setAuthenticationInfo( new AuthenticationInfo()
        {
            private static final long serialVersionUID = 1L;

            {
                setUserName( "deployment" );
                setPassword( "mj21op1" );
            }
        } );

        updateRequest.setTransferListener( new TransferListener()
        {

            private int col = 0;

            private int count = 0;

            public void transferStarted( final TransferEvent transferEvent )
            {
                System.out.println( "Started transfer: " + transferEvent.getResource().getName() );
            }

            public void transferProgress( final TransferEvent transferEvent, final byte[] buffer, final int length )
            {
                if ( count > 16384 )
                {
                    if ( col > 80 )
                    {
                        System.out.println();
                        col = 0;
                    }

                    System.out.print( '.' );
                    col++;
                    count = 0;
                }

                count += length;
            }

            public void transferInitiated( final TransferEvent transferEvent )
            {
            }

            public void transferError( final TransferEvent transferEvent )
            {
                System.out.println( "[ERROR]: " + transferEvent.getException().getLocalizedMessage() );
                transferEvent.getException().printStackTrace();
            }

            public void transferCompleted( final TransferEvent transferEvent )
            {
                System.out.println( "\nCompleted transfer: " + transferEvent.getResource().getName() );
            }

            public void debug( final String message )
            {
                System.out.println( "[DEBUG]: " + message );
            }
        } );

        container.lookup( IndexUpdater.class ).fetchAndUpdateIndex( updateRequest );
    }

}
