package org.sonatype.nexus.proxy.maven.uid;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.uid.Attribute;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeSource;

/**
 * The attributes implemented in Nexus Maven plugin contributing Maven specific UID attributes.
 * 
 * @author cstamas
 */
@Component( role = RepositoryItemUidAttributeSource.class, hint = "maven" )
public class MavenRepositoryItemUidAttributeSource
    implements RepositoryItemUidAttributeSource
{
    private final Map<Class<?>, Attribute<?>> attributes;

    public MavenRepositoryItemUidAttributeSource()
    {
        Map<Class<?>, Attribute<?>> attrs = new HashMap<Class<?>, Attribute<?>>( 6 );

        attrs.put( IsMavenArtifactAttribute.class, new IsMavenArtifactAttribute() );
        attrs.put( IsMavenSnapshotArtifactAttribute.class, new IsMavenSnapshotArtifactAttribute() );
        attrs.put( IsMavenChecksumAttribute.class, new IsMavenChecksumAttribute() );
        attrs.put( IsMavenPomAttribute.class, new IsMavenPomAttribute() );
        attrs.put( IsMavenRepositoryMetadataAttribute.class, new IsMavenRepositoryMetadataAttribute() );
        attrs.put( IsMavenArtifactSignatureAttribute.class, new IsMavenArtifactSignatureAttribute() );

        this.attributes = Collections.unmodifiableMap( attrs );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends Attribute<?>> T getAttribute( Class<T> attributeKey, RepositoryItemUid subject )
    {
        return (T) attributes.get( attributeKey );
    }
}
