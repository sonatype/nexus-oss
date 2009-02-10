package org.sonatype.nexus.jsecurity.realms;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.jsecurity.model.CPrivilege;
import org.sonatype.jsecurity.realms.privileges.AbstractPrivilegeDescriptor;
import org.sonatype.jsecurity.realms.privileges.PrivilegeDescriptor;
import org.sonatype.jsecurity.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.jsecurity.realms.validator.ValidationContext;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

@Component( role = PrivilegeDescriptor.class, hint = "RepositoryViewPrivilegeDescriptor" )
public class RepositoryViewPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    public static final String TYPE = "repository";
    
    @Requirement( role = PrivilegePropertyDescriptor.class, hint = "TargetPrivilegeRepositoryPropertyDescriptor" )
    private PrivilegePropertyDescriptor repoProperty;
    
    public String getName()
    {
        return "Repository View";
    }

    public List<PrivilegePropertyDescriptor> getPropertyDescriptors()
    {
        List<PrivilegePropertyDescriptor> propertyDescriptors = new ArrayList<PrivilegePropertyDescriptor>();
        
        propertyDescriptors.add( repoProperty );
        
        return propertyDescriptors;
    }

    public String getType()
    {
        return TYPE;
    }

    public String buildPermission( CPrivilege privilege )
    {
        if ( !TYPE.equals( privilege.getType() ) )
        {
            return null;
        }
         
        String repoId = getProperty( privilege, TargetPrivilegeRepositoryPropertyDescriptor.ID );
        
        if ( StringUtils.isEmpty( repoId ) )
        {
            repoId = "*";
        }
  
        return "nexus:repogroup:" + repoId;
    }
    
    @Override
    public ValidationResponse validatePrivilege( CPrivilege privilege, ValidationContext ctx, boolean update )
    {
        ValidationResponse response = super.validatePrivilege( privilege, ctx, update );
        
        if ( !TYPE.equals( privilege.getType() ) )
        {
            return response;
        }

        return response;
    }
    
    public static String buildPermission( String repoId )
    {
        return "nexus:repogroup:" + repoId;
    }
}
