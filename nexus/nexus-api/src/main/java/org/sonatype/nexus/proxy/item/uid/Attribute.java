package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public interface Attribute<T>
{
    T getValueFor( RepositoryItemUid subject );
}
