package org.sonatype.nexus.plugins.capabilities.api.descriptor;

public abstract class AbstractCapabilityPropertyDescriptor
    implements CapabilityPropertyDescriptor
{

    public boolean isRequired()
    {
        return true;
    }

    public String regexValidation()
    {
        return null;
    }

}
