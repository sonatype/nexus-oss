package org.sonatype.jsecurity.realms.privileges.application;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;

@Component( role = PrivilegePropertyDescriptor.class, hint = "ApplicationPrivilegeMethodPropertyDescriptor" )
public class ApplicationPrivilegeMethodPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
    public static final String ID = "method";
    
    public String getHelpText()
    {
        return "The method (create, read, update, delete) assigned to this privilege.";
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Method";
    }
    
    public String getType()
    {
        return "string";
    }
}
