package org.sonatype.nexus.plugins.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.plugins.rest.StaticResource;

public class ExternalStaticResource
    implements StaticResource
{
    private File file;
    private String path;
    private String contentType;
    
    public ExternalStaticResource( File file, String path, String contentType )
    {
        this.file = file;
        this.path = path;
        this.contentType = contentType;
    }
    
    public String getContentType()
    {
        return contentType;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return new FileInputStream( file );
    }

    public String getPath()
    {
        return path;
    }

    public long getSize()
    {
        return file.length();
    }
}
