package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * Core component doing "aggregation" of attribute sources, possibly contributed by plugins.
 * 
 * @author cstamas
 */
public interface RepositoryItemUidAttributeManager
{
    <T extends Attribute<?>> T getAttribute( Class<T> attributeKey, RepositoryItemUid subject );
}
