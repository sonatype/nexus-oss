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
package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;

import org.sonatype.nexus.plugins.rest.StaticResource;

/**
 * {@link StaticResource} contributed from a Nexus plugin.
 */
public final class PluginStaticResource
    implements StaticResource
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final URL resourceURL;

    private final String publishedPath;

    private final String contentType;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public PluginStaticResource( final URL resourceURL, final String publishedPath, final String contentType )
    {
        this.resourceURL = resourceURL;
        this.publishedPath = publishedPath;
        this.contentType = contentType;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public String getPath()
    {
        return publishedPath;
    }

    public String getContentType()
    {
        return contentType;
    }

    public long getSize()
    {
        try
        {
            return resourceURL.openConnection().getContentLength();
        }
        catch ( final Throwable e ) // NOPMD
        {
            // default to unknown size
        }
        return -1;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return resourceURL.openStream();
    }

    public Long getLastModified()
    {
        try
        {
            final URLConnection urlConn = resourceURL.openConnection();
            if ( urlConn instanceof JarURLConnection )
            {
                final JarEntry jarEntry = ( (JarURLConnection) urlConn ).getJarEntry();
                if ( jarEntry != null )
                {
                    return Long.valueOf( jarEntry.getTime() );
                }
                // This is a jar, not an entry in a jar
            }
            return Long.valueOf( urlConn.getLastModified() );
        }
        catch ( final Throwable e ) // NOPMD
        {
            return null; // default to unknown last modified time
        }
    }
}
