package org.sonatype.nexus;

import java.io.InputStream;

public class NexusStreamResponse
{
    private String name;
    
    private InputStream inputStream;

    private long size;

    private String mimeType;

    private long fromByte;

    private long bytesCount;

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream( InputStream inputStream )
    {
        this.inputStream = inputStream;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize( long contentLength )
    {
        this.size = contentLength;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType( String contentType )
    {
        this.mimeType = contentType;
    }

    public long getFromByte()
    {
        return fromByte;
    }

    public void setFromByte( long fromByte )
    {
        this.fromByte = fromByte;
    }

    public long getBytesCount()
    {
        return bytesCount;
    }

    public void setBytesCount( long bytesCount )
    {
        this.bytesCount = bytesCount;
    }
}
