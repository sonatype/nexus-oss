package org.sonatype.nexus.proxy.registry;

public class DefaultContentClass
    extends AbstractIdContentClass
{
    private final String id;

    public DefaultContentClass( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }
}
