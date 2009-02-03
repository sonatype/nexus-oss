package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;

@Component( role = PrivilegePropertyDescriptor.class, hint = "TargetPrivilegeRepositoryPropertyDescriptor" )
public class TargetPrivilegeRepositoryPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
    public static final String ID = "repositoryId";
    
    public String getHelpText()
    {
        return "The Repository associated with this Privilege.";
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Repository";
    }
}
