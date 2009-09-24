package org.sonatype.nexus.index.updater.fixtures;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;

public class TransferListenerFixture
    implements TransferListener
{
    private static final int ONE_CHUNK = 64;

    private int col = 0;

    private int count = 0;

    private int kb = 0;

    public void transferStarted( final TransferEvent transferEvent )
    {
        System.out.println( "Started transfer: " + transferEvent.getResource().getName() );
    }

    public void transferProgress( final TransferEvent transferEvent, final byte[] buffer, final int length )
    {
        if ( buffer == null )
        {
            return;
        }

        count += buffer.length;

        if ( ( count / ONE_CHUNK ) > kb )
        {
            if ( col > 80 )
            {
                System.out.println();
                col = 0;
            }

            System.out.print( '.' );
            col++;
            kb++;
        }
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
        System.out.println( "\nCompleted transfer: " + transferEvent.getResource().getName() + " ("
            + (double) ( count / ONE_CHUNK ) + " chunks of size: " + ONE_CHUNK + " bytes)" );
    }

    public void debug( final String message )
    {
        System.out.println( "[DEBUG]: " + message );
    }
}
