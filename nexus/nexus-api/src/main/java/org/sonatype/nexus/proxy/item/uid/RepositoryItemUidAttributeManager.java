package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public interface RepositoryItemUidAttributeManager
{
    <T extends Attribute<?>> T getAttribute( Class<T> attribute, RepositoryItemUid subject );
}
