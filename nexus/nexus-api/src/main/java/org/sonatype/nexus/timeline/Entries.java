package org.sonatype.nexus.timeline;

/**
 * Iterable of Entries.
 *
 * @author: cstamas
 * @since 1.10.0
 */
public interface Entries
    extends Iterable<Entry>
{

    /**
     * This method must be called to release underlying resources (implementation dependant).
     */
    void release();
}
