package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public interface RepositoryItemUidAttributeManager
{
    <T extends Attribute<?>> void registerAttribute( T attr );

    <T extends Attribute<?>> void deregisterAttribute( Class<T> attr );

    <T extends Attribute<?>> T getAttribute( Class<T> attribute, RepositoryItemUid subject );
}
