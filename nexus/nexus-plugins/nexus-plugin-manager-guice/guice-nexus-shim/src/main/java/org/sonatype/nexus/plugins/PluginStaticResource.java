/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
        catch ( final Exception e ) // NOPMD
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
}