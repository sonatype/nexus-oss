package org.sonatype.nexus.plugins.yum.plugin.client.subsystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class CompressionAdapter
{
    private final CompressionType compression;

    public CompressionAdapter( CompressionType compression )
    {
        this.compression = compression;
    }

    public InputStream adapt( InputStream inputStream )
        throws IOException
    {
        switch ( compression )
        {
            case NONE:
                return inputStream;
            case GZIP:
                return new GZIPInputStream( inputStream );
            case BZIP2:
                return new BZip2CompressorInputStream( inputStream );
            default:
                throw new IllegalArgumentException( "Could not adapt unknown compression " + compression );
        }
    }

}
