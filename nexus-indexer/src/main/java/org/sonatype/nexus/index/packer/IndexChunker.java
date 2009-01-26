/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.packer;

import java.util.Date;

import org.sonatype.nexus.index.ArtifactInfo;

/**
 * An index chunker converts timestamp (e.g. {@link ArtifactInfo#LAST_MODIFIED}) to corresponding index chunk name.
 * 
 * @author Tamas Cservenak
 */
public interface IndexChunker
{
    /**
     * Returns the chunk id corresponding to a given timestamp. Chunk id could be a string representation of the 
     * date suitable for using in file names.
     * 
     * @param d
     * @return
     */
    String getChunkId( Date d );

    /**
     * Returns the associated Date for given chunk id.
     * 
     * @param d
     * @return
     */
    Date getChunkDate( String id );
}
