/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * An OBR site that's referenced from another OBR by a referral tag.
 */
public class ReferencedObrSite
    extends AbstractObrSite
{
    private URL url;

    /**
     * Creates a referenced OBR site based on the given URL.
     * 
     * @param url the metadata URL
     */
    public ReferencedObrSite( URL url )
    {
        this.url = url;
    }

    public URL getMetadataUrl()
    {
        return url;
    }

    public String getMetadataPath()
    {
        return "";
    }

    @Override
    protected InputStream openRawStream()
        throws IOException
    {
        return url.openStream();
    }

    @Override
    protected String getContentType()
    {
        try
        {
            return url.openConnection().getContentType();
        }
        catch ( IOException e )
        {
            return null;
        }
    }
}
