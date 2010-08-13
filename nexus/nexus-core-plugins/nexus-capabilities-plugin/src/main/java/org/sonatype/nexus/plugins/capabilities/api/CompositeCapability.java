package org.sonatype.nexus.plugins.capabilities.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CompositeCapability
    extends AbstractCapability
{

    private final Collection<Capability> capabilities;

    public CompositeCapability( final String id )
    {
        super( id );
        capabilities = new ArrayList<Capability>();
    }

    public void add( final Capability capability )
    {
        capabilities.add( capability );
    }

    public void remove( final Capability capability )
    {
        capabilities.remove( capability );
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        for ( final Capability capability : capabilities )
        {
            capability.create( properties );
        }
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        for ( final Capability capability : capabilities )
        {
            capability.load( properties );
        }
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        for ( final Capability capability : capabilities )
        {
            capability.update( properties );
        }
    }

    @Override
    public void remove()
    {
        for ( final Capability capability : capabilities )
        {
            capability.remove();
        }
    }

}