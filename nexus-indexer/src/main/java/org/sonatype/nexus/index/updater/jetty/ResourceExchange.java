package org.sonatype.nexus.index.updater.jetty;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;

public class ResourceExchange
    extends ContentExchange
{

    protected byte[] _responseContentBytes;

    protected int _responseStatus;

    protected int _contentLength;

    protected String _contentEncoding;

    protected long _lastModified;

    public ResourceExchange( final HttpFields httpHeaders )
    {
        super( false );

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

    public int getContentLength()
    {
        return _contentLength;
    }

    public void setContentLength( final int length )
    {
        _contentLength = length;
    }

    public String getContentEncoding()
    {
        return _contentEncoding;
    }

    public void setContentEncoding( final String encoding )
    {
        _contentEncoding = encoding;
    }

    public long getLastModified()
    {
        return _lastModified;
    }

    public void setLastModified( final long time )
    {
        _lastModified = time;
    }

    public void setResponseStatus( final int status )
    {
        _responseStatus = status;
    }

    @Override
    public int getResponseStatus()
    {
        if ( _responseStatus != 0 )
        {
            return _responseStatus;
        }
        else
        {
            return super.getResponseStatus();
        }
    }

    public void setResponseContentBytes( final byte[] bytes )
    {
        _responseContentBytes = bytes;
    }

    public InputStream getResponseContentSource()
        throws UnsupportedEncodingException
    {
        return new ByteArrayInputStream( _responseContentBytes != null ? _responseContentBytes
                        : getResponseContentBytes() );
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
            case HttpHeaders.CONTENT_LENGTH_ORDINAL:
                _contentLength = BufferUtil.toInt( value );
                break;
            case HttpHeaders.CONTENT_ENCODING_ORDINAL:
                _contentEncoding = BufferUtil.to8859_1_String( value );
                break;
            case HttpHeaders.LAST_MODIFIED_ORDINAL:
                String lastModifiedStr = BufferUtil.to8859_1_String( value );
                _lastModified =
                    ( lastModifiedStr == null || lastModifiedStr.length() == 0 ? 0 : Date.parse( lastModifiedStr ) );
                break;
        }
    }
}
