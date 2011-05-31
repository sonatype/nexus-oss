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
