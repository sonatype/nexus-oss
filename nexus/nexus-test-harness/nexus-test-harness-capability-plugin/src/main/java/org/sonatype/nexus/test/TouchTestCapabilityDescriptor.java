package org.sonatype.nexus.test;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityPropertyDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.RepositoryOrGroupCapabilityPropertyDescriptor;

@Component( role = CapabilityDescriptor.class, hint = TouchTestCapability.ID )
public class TouchTestCapabilityDescriptor
    implements CapabilityDescriptor
{

    public String id()
    {
        return TouchTestCapability.ID;
    }

    public String name()
    {
        return "Touch Test Capability";
    }

    public CapabilityPropertyDescriptor[] propertyDescriptors()
    {
        return new CapabilityPropertyDescriptor[] { new RepositoryOrGroupCapabilityPropertyDescriptor(),
            new StringCapabilityPropertyDescriptor( "msg", "Message", true ) };
    }

    public boolean isExposed()
    {
        return true;
    }

}
