/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Date;

import org.codehaus.plexus.util.IOUtil;
import org.mortbay.jetty.EofException;
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

        setAvailable( true );

        if ( file.getAttributes().containsKey( DigestCalculatingInspector.DIGEST_SHA1_KEY ) )
        {
            setTag( new Tag( file.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ), false ) );
        }

        if ( file.getItemContext().containsKey( AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY ) )
        {
            String filename =
                file.getItemContext().get( AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY ).toString();

            setDownloadable( true );

            setDownloadName( filename );
        }

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
    public void write( OutputStream outputStream )
        throws IOException
    {
        InputStream is = null;

        try
        {
            is = getStorageFileItem().getInputStream();

            IOUtil.copy( is, outputStream );
        }
        catch ( EofException e )
        {
            // https://issues.sonatype.org/browse/NEXUS-217
        }
        catch ( SocketException e )
        {
            // https://issues.sonatype.org/browse/NEXUS-217
        }
        finally
        {
            IOUtil.close( is );
        }
    }

}
