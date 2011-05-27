/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.metadata;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.osgi.service.obr.Resource;

/**
 * An {@link Appendable} writer that appends OBR resources to a metadata stream.
 */
public interface ObrResourceWriter
    extends Appendable, Closeable, Flushable
{
    /**
     * Appends the given OBR resource to the underlying metadata stream.
     * 
     * @param resource the resource
     * @throws IOException
     */
    public void append( Resource resource )
        throws IOException;

    /**
     * Appends the OBR metadata footer and marks the stream as complete.
     */
    public void complete();
}
