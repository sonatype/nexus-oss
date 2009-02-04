package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;

@Component( role = PrivilegePropertyDescriptor.class, hint = "TargetPrivilegeGroupPropertyDescriptor" )
public class TargetPrivilegeGroupPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
    public static final String ID = "repositoryGroupId";
    
    public String getHelpText()
    {
        return "The Repository Group associated with this Privilege.";
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Repository Group";
    }
    
    public String getType()
    {
        return "repogroup";
    }
}
