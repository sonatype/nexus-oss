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
package org.sonatype.nexus.obr.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Abstract {@link ObrSite} that automatically detects compressed OBRs and inflates them.
 */
public abstract class AbstractObrSite
    implements ObrSite
{
    public final InputStream openStream()
        throws IOException
    {
        if ( "application/zip".equalsIgnoreCase( getContentType() ) )
        {
            final ZipInputStream zis = new ZipInputStream( openRawStream() );
            for ( ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry() )
            {
                // scan for the specific OBR entry
                final String name = e.getName().toLowerCase();
                if ( name.endsWith( "repository.xml" ) || name.endsWith( "obr.xml" ) )
                {
                    return zis;
                }
            }
            throw new IOException( "No repository.xml or obr.xml in zip: " + getMetadataUrl() );
        }

        return openRawStream();
    }

    /**
     * Opens a new raw stream to the OBR metadata, caller must close the stream.
     * 
     * @return a new input stream
     * @throws IOException
     */
    protected abstract InputStream openRawStream()
        throws IOException;

    /**
     * Retrieves the Content-Type of the OBR metadata.
     * 
     * @return OBR resource Content-Type
     */
    protected abstract String getContentType();
}
