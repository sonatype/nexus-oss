package org.sonatype.nexus.proxy.item.uid;

import java.util.concurrent.ConcurrentHashMap;

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
    private final ConcurrentHashMap<Class<?>, Object> attributes;

    public DefaultRepositoryItemUidAttributeManager()
    {
        super();

        attributes = new ConcurrentHashMap<Class<?>, Object>();

        fillInDefaults();
    }

    protected void fillInDefaults()
    {
        registerAttribute( new IsHiddenUidAttribute() );

        registerAttribute( new IsMetadataMaintainedAttribute() );
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Attribute<?>> T getAttribute( final Class<T> attribute, final RepositoryItemUid subject )
    {
        return (T) attributes.get( attribute );
    }

    public <T extends Attribute<?>> void registerAttribute( T attr )
    {
        attributes.put( attr.getClass(), attr );
    }

    public <T extends Attribute<?>> void deregisterAttribute( Class<T> attr )
    {
        attributes.remove( attr );
    }
}
