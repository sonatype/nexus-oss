package org.sonatype.nexus.proxy.item.uid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * The source for attributes implemented in Nexus Core.
 * 
 * @author cstamas
 */
@Component( role = RepositoryItemUidAttributeSource.class, hint = "core" )
public class CoreRepositoryItemUidAttributeSource
    implements RepositoryItemUidAttributeSource
{
    private final Map<Class<?>, Attribute<?>> coreAttributes;

    public CoreRepositoryItemUidAttributeSource()
    {
        Map<Class<?>, Attribute<?>> attrs = new HashMap<Class<?>, Attribute<?>>( 2 );

        attrs.put( IsHiddenUidAttribute.class, new IsHiddenUidAttribute() );
        attrs.put( IsMetadataMaintainedAttribute.class, new IsMetadataMaintainedAttribute() );

        this.coreAttributes = Collections.unmodifiableMap( attrs );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends Attribute<?>> T getAttribute( Class<T> attributeKey, RepositoryItemUid subject )
    {
        return (T) coreAttributes.get( attributeKey );
    }
}
