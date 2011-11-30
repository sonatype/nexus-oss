/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;

public class StorageFileItemRepresentation
    extends StorageItemRepresentation
{
    public StorageFileItemRepresentation( StorageFileItem file )
    {
        super( MediaType.valueOf( file.getMimeType() ), file );

        setSize( file.getLength() );

        if ( file.getRepositoryItemAttributes().containsKey( DigestCalculatingInspector.DIGEST_SHA1_KEY ) )
        {
            // Shield SHA1
            // {SHA1{xxxx}}
            final String tag =
                String.format( "{SHA1{%s}}",
                    file.getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );
            setTag( new Tag( tag, false ) );
        }

        if ( file.getItemContext().containsKey( AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY ) )
        {
            String filename =
                file.getItemContext().get( AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY ).toString();

            setDownloadable( true );

            setDownloadName( filename );
        }
    }

    protected StorageFileItem getStorageItem()
    {
        return (StorageFileItem) super.getStorageItem();
    }

    public boolean isTransient()
    {
        return !getStorageItem().isReusableStream();
    }

    @Override
    public void write( OutputStream outputStream )
        throws IOException
    {
        InputStream is = null;

        try
        {
            is = getStorageItem().getInputStream();

            IOUtil.copy( is, outputStream );
        }
        catch ( IOException e )
        {
            if ( "EofException".equals( e.getClass().getSimpleName() ) )
            {
                // This is for Jetty's org.eclipse.jetty.io.EofException
                // https://issues.sonatype.org/browse/NEXUS-217
            }
            else if ( e instanceof SocketException )
            {
                // https://issues.sonatype.org/browse/NEXUS-217
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            IOUtil.close( is );
        }
    }
}
