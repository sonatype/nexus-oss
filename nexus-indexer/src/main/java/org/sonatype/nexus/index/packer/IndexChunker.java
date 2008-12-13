package org.sonatype.nexus.index.packer;

import java.util.Date;

/**
 * A component responsible to cut index into chunks.
 * 
 * @author cstamas
 */
public interface IndexChunker
{
    /**
     * Every chunker has an unique ID.
     * 
     * @return
     */
    String getId();

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
