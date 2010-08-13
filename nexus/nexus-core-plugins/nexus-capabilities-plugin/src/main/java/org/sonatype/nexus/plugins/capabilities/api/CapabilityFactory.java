package org.sonatype.nexus.plugins.capabilities.api;

import org.sonatype.plugin.ExtensionPoint;

@ExtensionPoint
public interface CapabilityFactory
{

    Capability create( String id );

}
