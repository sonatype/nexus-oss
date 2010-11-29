package org.sonatype.nexus.proxy.item.uid;

import org.codehaus.plexus.component.annotations.Component;
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
    @SuppressWarnings( "unchecked" )
    public <T extends Attribute<?>> T getAttribute( Class<T> attribute, RepositoryItemUid subject )
    {
        if ( attribute.equals( HiddenUid.class ) )
        {
            return (T) new HiddenUid( subject );
        }
        else
        {
            return null;
        }
    }
}
