/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.obr.metadata;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.osgi.service.obr.Resource;

/**
 * An {@link XmlPullParser} that knows how to parse OBR resources.
 */
public interface ObrParser
    extends XmlPullParser, Closeable
{
    /**
     * Returns the URL of the OBR metadata, may be local (file:) or remote.
     * 
     * @return the metadata URL
     */
    URL getMetadataUrl();

    /**
     * Returns the maximum allowed depth of nested OBR referrals.
     * 
     * @return the maximum depth
     */
    int getMaxDepth();

    /**
     * Parses an OBR resource from the underlying OBR metadata stream.
     * 
     * @return the parsed resource
     * @throws IOException
     */
    Resource parseResource()
        throws IOException;
}
