/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Access details for OBR metadata hosted on a local or remote site.
 */
public interface ObrSite
{
    /**
     * Returns the URL of the OBR metadata, may be local (file:) or remote.
     * 
     * @return the metadata URL
     */
    URL getMetadataUrl();

    /**
     * Returns the path to the OBR metadata, relative to the hosting site.
     * 
     * @return the relative path
     */
    String getMetadataPath();

    /**
     * Opens a new stream to the OBR metadata, caller must close the stream.
     * 
     * @return a new input stream
     * @throws IOException
     */
    InputStream openStream()
        throws IOException;
}
