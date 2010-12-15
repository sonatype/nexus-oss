package org.sonatype.nexus.proxy.item.uid;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * This is just a quick implementation for currently only one existing attribute: is hidden. Later this should be
 * extended.
 * 
 * @author cstamas
 */
@Component( role = RepositoryItemUidAttributeManager.class )
public class DefaultRepositoryItemUidAttributeManager
    implements RepositoryItemUidAttributeManager
{
    @Requirement( role = RepositoryItemUidAttributeSource.class )
    private Map<String, RepositoryItemUidAttributeSource> attributeSources;

    public <T extends Attribute<?>> T getAttribute( final Class<T> attributeKey, final RepositoryItemUid subject )
    {
        for ( RepositoryItemUidAttributeSource attributeSource : attributeSources.values() )
        {
            T attribute = attributeSource.getAttribute( attributeKey, subject );

            if ( attribute != null )
            {
                return attribute;
            }
        }

        return null;
    }
}
