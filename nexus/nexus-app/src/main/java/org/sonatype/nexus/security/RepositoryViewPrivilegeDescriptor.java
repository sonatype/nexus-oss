package org.sonatype.nexus.security;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeRepositoryPropertyDescriptor;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.realms.privileges.AbstractPrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.realms.validator.SecurityValidationContext;

@Component( role = PrivilegeDescriptor.class, hint = "RepositoryViewPrivilegeDescriptor" )
public class RepositoryViewPrivilegeDescriptor
    extends AbstractPrivilegeDescriptor
    implements PrivilegeDescriptor
{
    public static final String TYPE = "repository";

    @Requirement( role = PrivilegePropertyDescriptor.class, hint = "RepositoryPropertyDescriptor" )
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

        String repoId = getProperty( privilege, RepositoryPropertyDescriptor.ID );

        if ( StringUtils.isEmpty( repoId ) )
        {
            repoId = "*";
        }

        return "nexus:repoview:" + repoId;
    }

    @Override
    public ValidationResponse<SecurityValidationContext> validatePrivilege( CPrivilege privilege,
        SecurityValidationContext ctx, boolean update )
    {
        ValidationResponse<SecurityValidationContext> response = super.validatePrivilege( privilege, ctx, update );

        if ( !TYPE.equals( privilege.getType() ) )
        {
            return response;
        }

        return response;
    }

    public static String buildPermission( String repoId )
    {
        return "nexus:repoview:" + repoId;
    }
}
