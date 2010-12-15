package org.sonatype.nexus.proxy.item.uid;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.plugin.ExtensionPoint;

import com.google.inject.Singleton;

/**
 * RepositoryItemUid Attribute source that is contributing attributes to core.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
public interface RepositoryItemUidAttributeSource
{
    /**
     * This method should return the attribute corresponding to attributeKey or return null if key is not known for it.
     * 
     * @return
     */
    <T extends Attribute<?>> T getAttribute( Class<T> attributeKey, RepositoryItemUid subject );
}
