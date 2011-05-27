/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.metadata;

import java.io.Closeable;
import java.io.IOException;

import org.osgi.service.obr.Resource;

/**
 * A {@link Readable} reader that pulls OBR resources from a metadata stream.
 */
public interface ObrResourceReader
    extends Readable, Closeable
{
    /**
     * Attempts to read an OBR resource from the underlying metadata stream.
     * 
     * @return the read resource
     * @throws IOException
     */
    public Resource readResource()
        throws IOException;
}
