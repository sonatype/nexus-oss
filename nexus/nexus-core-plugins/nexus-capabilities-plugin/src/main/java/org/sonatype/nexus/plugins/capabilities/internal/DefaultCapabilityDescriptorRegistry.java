package org.sonatype.nexus.plugins.capabilities.internal;

import java.util.List;

import javax.inject.Singleton;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;

@Singleton
public class DefaultCapabilityDescriptorRegistry
    implements CapabilityDescriptorRegistry
{

    // TODO temporary. To be replaced when new container inplace
    @Requirement( role = CapabilityDescriptor.class )
    private List<CapabilityDescriptor> descriptors;

    public CapabilityDescriptor get( final String capabilityDescriptorId )
    {
        for ( final CapabilityDescriptor descriptor : descriptors )
        {
            if ( descriptor.id().equals( capabilityDescriptorId ) )
            {
                return descriptor;
            }
        }
        return null;
    }

    public CapabilityDescriptor[] getAll()
    {
        return descriptors.toArray( new CapabilityDescriptor[descriptors.size()] );
    }
}
