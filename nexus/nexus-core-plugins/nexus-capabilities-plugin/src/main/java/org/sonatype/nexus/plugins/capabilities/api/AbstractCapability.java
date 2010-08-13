package org.sonatype.nexus.plugins.capabilities.api;

import java.util.Map;

public abstract class AbstractCapability
    implements Capability
{

    private final String id;

    protected AbstractCapability( final String id )
    {
        assert id != null : "Capability id cannot be null";

        this.id = id;
    }

    public String id()
    {
        return id;
    }

    public void create( final Map<String, String> properties )
    {
        // do nothing
    }

    public void load( final Map<String, String> properties )
    {
        // do nothing
    }

    public void update( final Map<String, String> properties )
    {
        // do nothing
    }

    public void remove()
    {
        // do nothing
    }

}
