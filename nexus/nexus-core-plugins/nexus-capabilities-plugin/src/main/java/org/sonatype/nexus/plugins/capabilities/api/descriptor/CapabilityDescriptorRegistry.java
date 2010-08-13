package org.sonatype.nexus.plugins.capabilities.api.descriptor;

import org.sonatype.plugin.Managed;

@Managed
public interface CapabilityDescriptorRegistry
{

    CapabilityDescriptor get( String capabilityDescriptorId );

    CapabilityDescriptor[] getAll();

}
