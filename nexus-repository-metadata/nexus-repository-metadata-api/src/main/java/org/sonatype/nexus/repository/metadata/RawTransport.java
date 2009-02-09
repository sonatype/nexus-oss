/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.repository.metadata;

import java.io.IOException;

/**
 * An interface defining raw resource downloading and uploading contract. This transport is suitable to move payloads
 * that are small enough to not be streamed. Just like repository metadata.
 * 
 * @author Eugene Kuleshov
 * @author cstamas
 */
public interface RawTransport
{
    /**
     * Retrieve the raw content of the path from repository.
     * 
     * @param name a name of resource to retrieve
     * @param targetFile a target file to save retrieved resource to
     */
    byte[] readRawData( RawTransportRequest request )
        throws Exception;

    /**
     * Writes raw content to the path in repository.
     * 
     * @param path
     * @param data
     * @throws IOException
     */
    void writeRawData( RawTransportRequest request, byte[] data )
        throws Exception;
}
