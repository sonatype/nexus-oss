/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
 */
package org.sonatype.nexus.index.packer;

import java.util.Date;

/**
 * A component responsible to cut index into chunks.
 * 
 * @author Tamas Cservenak
 */
public interface IndexChunker
{
    /**
     * Returns the chunkId (which is a date in string format, suitable for using in file names), where this document
     * should be.
     * 
     * @param d
     * @return
     */
    String getChunkId( Date d );

    /**
     * Returns the associated Date with the chunk ID.
     * 
     * @param d
     * @return
     */
    Date getChunkDate( String id );
}
