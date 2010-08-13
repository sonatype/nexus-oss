package org.sonatype.nexus.plugins.capabilities.api.descriptor;

public class RepositoryOrGroupCapabilityPropertyDescriptor
    extends AbstractCapabilityPropertyDescriptor
{

    public static final String ID = "repositoryOrGroupId";

    public String id()
    {
        return ID;
    }

    public String name()
    {
        return "Select the repository or repository group to which this capability applies.";
    }

    public String type()
    {
        return "repo-or-group";
    }

}
