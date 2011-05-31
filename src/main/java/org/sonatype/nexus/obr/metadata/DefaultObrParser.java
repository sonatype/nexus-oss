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
import java.net.URL;

import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.osgi.impl.bundle.obr.resource.RepositoryImpl;
import org.osgi.impl.bundle.obr.resource.ResourceImpl;
import org.osgi.service.obr.Resource;
import org.sonatype.nexus.proxy.InvalidItemContentException;

public class DefaultObrParser
    extends MXParser
    implements ObrParser
{
    private final URL metadataUrl;

    private final int maxDepth;

    private final RepositoryImpl obr;

    public DefaultObrParser( final ObrSite site, final int maxDepth, final boolean relative )
        throws XmlPullParserException, IOException
    {
        setInput( new XmlStreamReader( site.openStream() ) );

        metadataUrl = site.getMetadataUrl();
        this.maxDepth = maxDepth;

        // only allow absolute context for remote OBRs
        URL contextUrl = metadataUrl;
        if ( relative || "file".equals( contextUrl.getProtocol() ) )
        {
            contextUrl = new URL( "file:" + site.getMetadataPath() );
        }

        obr = new RepositoryImpl( contextUrl );
    }

    public URL getMetadataUrl()
    {
        return metadataUrl;
    }

    public int getMaxDepth()
    {
        return maxDepth;
    }

    public Resource parseResource()
        throws IOException
    {
        try
        {
            return new ResourceImpl( obr, this );
        }
        catch ( final XmlPullParserException e )
        {
            throw new InvalidItemContentException( "Error parsing OBR resource", e );
        }
    }

    public void close()
        throws IOException
    {
        reader.close();
    }
}
