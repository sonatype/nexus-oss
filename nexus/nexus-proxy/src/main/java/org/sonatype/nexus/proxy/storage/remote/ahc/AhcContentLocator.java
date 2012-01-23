/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
