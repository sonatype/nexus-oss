package org.sonatype.nexus.index.updater.jetty;

import org.apache.maven.wagon.events.TransferEvent;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.nio.NIOBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import edu.emory.mathcs.backport.java.util.Arrays;

public class ResourceExchange
    extends ContentExchange
{

    private final File targetFile;

    private final String originalUrl;

    private final TransferListenerSupport listenerSupport;

    protected long lastModified;

    private boolean redirectionRequested;

    private String redirectionUrl;

    private FileChannel targetChannel;

    private File tmpFile;

    private TransferEvent transferEvent;

    private String contentEncoding;

    private int responseCode;

    private int contentLength;

    public ResourceExchange( final File targetFile, final HttpFields httpHeaders, final String targetUrl,
                             final TransferListenerSupport listenerSupport )
    {
        this( targetFile, httpHeaders, targetUrl, targetUrl, listenerSupport );
    }

    public ResourceExchange( final File targetFile, final HttpFields httpHeaders, final String originalUrl,
                             final String targetUrl, final TransferListenerSupport listenerSupport )
    {
        super( false );

        setMethod( HttpMethods.GET );
        setURL( targetUrl );

        this.targetFile = targetFile;
        this.originalUrl = originalUrl;
        this.listenerSupport = listenerSupport;

        addRequestHeaders( httpHeaders );
    }

    private void addRequestHeaders( final HttpFields headers )
    {
        if ( headers != null )
        {
            for ( Enumeration<String> names = headers.getFieldNames(); names.hasMoreElements(); )
            {
                String name = names.nextElement();
                addRequestHeader( name, headers.getStringField( name ) );
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public void onResponseHeader( final Buffer name, final Buffer value )
        throws IOException
    {
        super.onResponseHeader( name, value );
        int header = HttpHeaders.CACHE.getOrdinal( name );
        switch ( header )
        {
            case HttpHeaders.CONTENT_ENCODING_ORDINAL:
            {
                contentEncoding = value.toString();
                break;
            }
            case HttpHeaders.CONTENT_LENGTH_ORDINAL:
            {
                contentLength = Integer.parseInt( value.toString() );
                break;
            }
            case HttpHeaders.LAST_MODIFIED_ORDINAL:
            {
                String lastModifiedStr = BufferUtil.to8859_1_String( value );
                lastModified =
                    ( lastModifiedStr == null || lastModifiedStr.length() == 0 ? 0 : Date.parse( lastModifiedStr ) );
                break;
            }
            case HttpHeaders.LOCATION_ORDINAL:
            {
                redirectionUrl = value.toString();
                break;
            }
        }
    }

    @Override
    public void onResponseStatus( final Buffer version, final int status, final Buffer reason )
        throws IOException
    {
        super.onResponseStatus( version, status, reason );
        if ( status == ServerResponse.SC_MOVED_PERMANENTLY || status == ServerResponse.SC_MOVED_TEMPORARILY )
        {
            redirectionRequested = true;
        }
    }

    @Override
    protected synchronized void onResponseContent( final Buffer content )
        throws IOException
    {
        if ( !redirectionRequested )
        {
            if ( targetChannel == null )
            {
                File destinationDirectory = targetFile.getParentFile();
                if ( destinationDirectory != null && !destinationDirectory.exists() )
                {
                    destinationDirectory.mkdirs();
                    if ( !destinationDirectory.exists() )
                    {
                        throw new IOException( "Specified destination directory cannot be created: "
                            + destinationDirectory );
                    }
                }

                newTempFile();
                targetChannel = new FileOutputStream( tmpFile ).getChannel();

                listenerSupport.fireGetStarted( originalUrl, targetFile );

                transferEvent =
                    new JettyTransferEvent( originalUrl, TransferEvent.TRANSFER_PROGRESS, TransferEvent.REQUEST_GET );

            }

            if ( content instanceof NIOBuffer )
            {
                targetChannel.write( ( (NIOBuffer) content ).getByteBuffer() );
            }
            else
            {
                targetChannel.write( ByteBuffer.wrap( content.asArray() ) );
            }

            transferEvent.setTimestamp( System.currentTimeMillis() );
            listenerSupport.fireTransferProgress( transferEvent, content.array(), contentLength );
        }
        else
        {
            System.out.println( "Redirection requested for location: " + redirectionUrl + "\nSkipping response body." );
        }
    }

    private void newTempFile()
    {
        tmpFile = new File( targetFile.getAbsolutePath() + ".tmp" );
    }

    @Override
    protected void onResponseComplete()
        throws IOException
    {
        if ( targetChannel != null && targetChannel.isOpen() )
        {
            targetChannel.close();

            if ( "gzip".equals( contentEncoding ) )
            {
                System.out.println( "Unpacking: " + tmpFile + "\nto: " + targetFile );

                InputStream in = null;
                OutputStream out = null;
                try
                {
                    in = new GZIPInputStream( new FileInputStream( tmpFile ) );
                    out = new FileOutputStream( targetFile );

                    listenerSupport.fireDebug( "Unpacking GZIP content to destination: " + targetFile );
                    IOUtil.copy( in, out );
                }
                finally
                {
                    IOUtil.close( in );
                    IOUtil.close( out );
                }
            }
            else
            {
                System.out.println( "Moving: " + tmpFile + "\nto: " + targetFile );
                if ( targetFile.exists() )
                {
                    targetFile.delete();
                }

                tmpFile.renameTo( targetFile );
                System.out.println( "Move target exists? " + targetFile.exists() );
                targetFile.setLastModified( lastModified );
            }

            tmpFile = null;

            listenerSupport.fireGetCompleted( originalUrl, targetFile );
            transferEvent = null;
            redirectionRequested = false;
        }
    }

    void setResponseContentStream( final InputStream in )
        throws IOException
    {
        if ( targetChannel != null )
        {
            if ( targetChannel.isOpen() )
            {
                targetChannel.close();
            }

            targetChannel = null;
            if ( tmpFile.exists() )
            {
                tmpFile.delete();
            }
        }

        newTempFile();

        OutputStream out = null;
        try
        {
            out = new FileOutputStream( tmpFile );

            transferEvent =
                new JettyTransferEvent( originalUrl, TransferEvent.TRANSFER_PROGRESS, TransferEvent.REQUEST_GET );

            byte[] buf = new byte[4096];
            int read = -1;
            while ( ( read = in.read( buf ) ) > -1 )
            {
                transferEvent.setTimestamp( System.currentTimeMillis() );
                listenerSupport.fireTransferProgress( transferEvent, Arrays.copyOf( buf, read ), contentLength );

                out.write( buf, 0, read );
            }

            IOUtil.copy( in, out );
            out.flush();
        }
        finally
        {
            IOUtil.close( out );
        }
    }

    void setLastModified( final long lastModified )
    {
        this.lastModified = lastModified;
    }

    void setResponseStatus( final int responseCode )
    {
        this.responseCode = responseCode;
    }

    @Override
    public int getResponseStatus()
    {
        if ( responseCode > 0 )
        {
            return responseCode;
        }

        return super.getResponseStatus();
    }

    void setContentEncoding( final String contentEncoding )
    {
        this.contentEncoding = contentEncoding;
    }

    void setContentLength( final int contentLength )
    {
        this.contentLength = contentLength;
    }

    public String getOriginalUrl()
    {
        return originalUrl;
    }

    public String getRedirectUrl()
    {
        return redirectionUrl;
    }

    public boolean isRedirectRequested()
    {
        return redirectionRequested;
    }

}
