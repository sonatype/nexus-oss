package org.sonatype.jsecurity.realms.privileges.application;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;

@Component( role = PrivilegePropertyDescriptor.class, hint = "ApplicationPrivilegePermissionPropertyDescriptor" )
public class ApplicationPrivilegePermissionPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
    public static final String ID = "permission";
    
    public String getHelpText()
    {
        return "The JSecurity permission string associated with this privilege";
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Permission";
    }
    
    public String getType()
    {
        return "string";
    }

}
