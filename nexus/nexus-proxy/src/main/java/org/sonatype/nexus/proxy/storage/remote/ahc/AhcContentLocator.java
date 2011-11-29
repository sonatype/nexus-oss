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
package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.item.ContentLocator;

import com.ning.http.client.AsyncHttpClient;

/**
 * ContentLocator backed by AsyncHttpClient offering multiple strategies: reusable and non-reusable. When reusable. it
 * actually performs HTTP GET on every getContent() invocation.
 * 
 * @author cstamas
 * @deprecated This class is not used anymore, but we may resurrect it, since it does introduce <em>reusable</em>
 *             ContentLocator.
 */
public class AhcContentLocator
    implements ContentLocator
{
    private final AsyncHttpClient client;

    private final String itemUrl;

    private final long length;

    private final long lastModified;

    private final String mimeType;

    private final InputStream inputStream;

    public AhcContentLocator( final AsyncHttpClient client, final String itemUrl, final long length,
                              final long lastModified, final String mimeType, final InputStream is )
    {
        this.client = client;
        this.itemUrl = itemUrl;
        this.length = length;
        this.lastModified = lastModified;
        this.mimeType = mimeType;
        this.inputStream = is;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        if ( inputStream != null )
        {
            return inputStream;
        }
        else
        {
            return AHCUtils.fetchContent( client, itemUrl );
        }
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public boolean isReusable()
    {
        return inputStream == null;
    }

    public long getLength()
    {
        return length;
    }

    public String getItemUrl()
    {
        return itemUrl;
    }

    public long getLastModified()
    {
        return lastModified;
    }
}
