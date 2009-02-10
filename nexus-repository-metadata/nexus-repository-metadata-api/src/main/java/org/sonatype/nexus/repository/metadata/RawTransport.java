/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.repository.metadata;

/**
 * An interface defining raw resource IO contract. This transport is suitable to move payloads that are small enough to
 * not be streamed. Just like repository metadata.
 * 
 * @author Eugene Kuleshov
 * @author cstamas
 */
public interface RawTransport
{
    /**
     * Retrieve the raw content of the path from repository. If the path is not found, null should be returned.
     * 
     * @param request
     * @return the raw data from path, or null if not found.
     */
    byte[] readRawData( String path )
        throws Exception;

    /**
     * Write the raw content to the path in repository.
     * 
     * @param request
     * @return the raw data from path, or null if not found.
     */
    void writeRawData( String path, byte[] data )
        throws Exception;
}
