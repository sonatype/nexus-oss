package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;

@Component( role = PrivilegePropertyDescriptor.class, hint = "TargetPrivilegeRepositoryTargetPropertyDescriptor" )
public class TargetPrivilegeRepositoryTargetPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
    public static final String ID = "repositoryTargetId";
    
    public String getHelpText()
    {
        return "The Repository Target associated with this Privilege.";
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Repository Target";
    }
}
