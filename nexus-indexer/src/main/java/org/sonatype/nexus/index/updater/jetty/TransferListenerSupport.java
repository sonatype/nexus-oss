package org.sonatype.nexus.index.updater.jetty;

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferEventSupport;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;

import java.io.File;

class TransferListenerSupport
{
    TransferEventSupport transferEventSupport = new TransferEventSupport();

    public void addTransferListener( final TransferListener listener )
    {
        transferEventSupport.addTransferListener( listener );
    }

    void fireTransferError( final String url, final Exception e, final int requestType )
    {
        TransferEvent transferEvent = new TransferEvent( null, resourceFor( url ), e, requestType );

        transferEventSupport.fireTransferError( transferEvent );
    }

    void fireTransferProgress( final TransferEvent transferEvent, final byte[] buffer, final int n )
    {
        transferEventSupport.fireTransferProgress( transferEvent, buffer, n );
    }

    void fireGetCompleted( final String url, final File localFile )
    {
        long timestamp = System.currentTimeMillis();

        TransferEvent transferEvent =
            new TransferEvent( null, resourceFor( url ), TransferEvent.TRANSFER_COMPLETED, TransferEvent.REQUEST_GET );

        transferEvent.setTimestamp( timestamp );

        transferEvent.setLocalFile( localFile );

        transferEventSupport.fireTransferCompleted( transferEvent );
    }

    Resource resourceFor( final String url )
    {
        Resource res = new Resource();
        res.setName( url );

        return res;
    }

    void fireGetStarted( final String url, final File localFile )
    {
        long timestamp = System.currentTimeMillis();

        TransferEvent transferEvent =
            new TransferEvent( null, resourceFor( url ), TransferEvent.TRANSFER_STARTED, TransferEvent.REQUEST_GET );

        transferEvent.setTimestamp( timestamp );

        transferEvent.setLocalFile( localFile );

        transferEventSupport.fireTransferStarted( transferEvent );
    }

    void fireGetInitiated( final String url, final File localFile )
    {
        long timestamp = System.currentTimeMillis();

        TransferEvent transferEvent =
            new TransferEvent( null, resourceFor( url ), TransferEvent.TRANSFER_INITIATED, TransferEvent.REQUEST_GET );

        transferEvent.setTimestamp( timestamp );

        transferEvent.setLocalFile( localFile );

        transferEventSupport.fireTransferInitiated( transferEvent );
    }

}
