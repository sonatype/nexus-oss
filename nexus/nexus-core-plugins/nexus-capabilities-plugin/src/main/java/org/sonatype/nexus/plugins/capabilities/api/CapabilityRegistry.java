package org.sonatype.nexus.plugins.capabilities.api;

import org.sonatype.plugin.Managed;

@Managed
public interface CapabilityRegistry
{

    void add( Capability capability );

    void remove( String capabilityId );

    Capability get( String capabilityId );

    Capability create( String capabilityId, String capabilityType );

}
