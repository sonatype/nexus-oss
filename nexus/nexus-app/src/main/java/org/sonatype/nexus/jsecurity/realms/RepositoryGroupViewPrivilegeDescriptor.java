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

@Component( role = PrivilegeDescriptor.class, hint = "RepositoryGroupViewPrivilegeDescriptor" )
public class RepositoryGroupViewPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    public static final String TYPE = "repoGroup";
    
    @Requirement( role = PrivilegePropertyDescriptor.class, hint = "TargetPrivilegeGroupPropertyDescriptor" )
    private PrivilegePropertyDescriptor groupProperty;
    
    public String getName()
    {
        return "Repository Group View";
    }

    public List<PrivilegePropertyDescriptor> getPropertyDescriptors()
    {
        List<PrivilegePropertyDescriptor> propertyDescriptors = new ArrayList<PrivilegePropertyDescriptor>();
        
        propertyDescriptors.add( groupProperty );
        
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
         
        String groupId = getProperty( privilege, TargetPrivilegeGroupPropertyDescriptor.ID );
        
        if ( StringUtils.isEmpty( groupId ) )
        {
            groupId = "*";
        }
  
        return "nexus:repogroup:" + groupId;
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
    
    public static String buildPermission( String groupId )
    {
        return "nexus:repogroup:" + groupId;
    }
}
