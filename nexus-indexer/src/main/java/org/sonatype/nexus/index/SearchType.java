package org.sonatype.nexus.index;

/**
 * Designated search types that NexusIndexer supports.
 * 
 * @author cstamas
 */
public enum SearchType
{
    /**
     * Scored search types are usually meant for human query input, where loose-matching and result ranking is
     * happening. The order of index hits is important, since it will reflect the hits ordered by score (1st hit, best
     * match, last hit worst).
     */
    SCORED,

    /**
     * Keyword search types are usually meant for applications filtering index content for some exact filtering
     * condition even in a "future proof" way (example with packaging "maven-archetype" vs "foo-archetype-maven").
     */
    KEYWORD;

    public boolean matchesIndexerField( IndexerField field )
    {
        switch ( this )
        {
            case SCORED:
                return !field.isKeyword();

            case KEYWORD:
                return field.isKeyword();

            default:
                return false;
        }
    }
}
