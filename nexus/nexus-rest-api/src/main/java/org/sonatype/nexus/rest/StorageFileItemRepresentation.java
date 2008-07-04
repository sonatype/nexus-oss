package org.sonatype.nexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.resource.OutputRepresentation;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class StorageFileItemRepresentation
    extends OutputRepresentation
{
    private final StorageFileItem file;

    public StorageFileItemRepresentation( StorageFileItem file )
    {
        super( MediaType.valueOf( file.getMimeType() ) );

        this.file = file;

        setSize( file.getLength() );

        setModificationDate( new Date( file.getModified() ) );

        if ( file.getAttributes().containsKey( DigestCalculatingInspector.DIGEST_SHA1_KEY ) )
        {
            setTag( new Tag( file.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) ) );
        }

        setAvailable( true );
    }

    protected StorageFileItem getStorageFileItem()
    {
        return file;
    }

    public boolean isTransient()
    {
        return !getStorageFileItem().isReusableStream();
    }

    @Override
    public InputStream getStream()
        throws IOException
    {
        return getStorageFileItem().getInputStream();
    }

    @Override
    public void write( OutputStream outputStream )
        throws IOException
    {
        InputStream is = null;

        try
        {
            is = getStorageFileItem().getInputStream();

            IOUtil.copy( is, outputStream );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

}
