package org.sonatype.nexus.plugins.capabilities.internal;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;

@Singleton
public class DefaultCapabilityRegistry
    implements CapabilityRegistry
{

    // TODO temporary. To be replaced when new container inplace
    @Requirement( role = CapabilityFactory.class )
    private Map<String, CapabilityFactory> factories;

    private final Map<String, Capability> capabilities;

    public DefaultCapabilityRegistry()
    {
        capabilities = new HashMap<String, Capability>();
    }

    public void add( final Capability capability )
    {
        assert capability != null : "Capability cannot be null";
        assert capability.id() != null : "Capability id cannot be null";

        capabilities.put( capability.id(), capability );
    }

    public Capability get( final String capabilityId )
    {
        return capabilities.get( capabilityId );
    }

    public void remove( final String capabilityId )
    {
        capabilities.remove( capabilityId );
    }

    public Capability create( final String capabilityId, final String capabilityType )
    {
        assert capabilityId != null : "Capability id cannot be null";

        final CapabilityFactory factory = factories.get( capabilityType );
        if ( factory == null )
        {
            throw new RuntimeException( String.format( "No factory found for a capability of type %s", capabilityType ) );
        }

        final Capability capability = factory.create( capabilityId );

        return capability;
    }

}
