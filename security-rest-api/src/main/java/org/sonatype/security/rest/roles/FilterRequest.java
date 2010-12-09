package org.sonatype.security.rest.roles;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.rest.model.RoleAndPrivilegeListResource;

public class FilterRequest
{
    private final boolean showPrivileges;

    private final boolean showRoles;

    private final boolean showExternalRoles;

    private final String text;

    private final List<String> roleIds;

    private final List<String> privilegeIds;

    public FilterRequest( boolean showPrivileges, boolean showRoles, boolean showExternalRoles, String text,
                          List<String> roleIds, List<String> privilegeIds )
    {
        this.showPrivileges = showPrivileges;
        this.showRoles = showRoles;
        this.showExternalRoles = showExternalRoles;
        this.text = text;
        this.roleIds = roleIds;
        this.privilegeIds = privilegeIds;
    }

    public boolean isShowPrivileges()
    {
        return showPrivileges;
    }

    public boolean isShowRoles()
    {
        return showRoles;
    }

    public boolean isShowExternalRoles()
    {
        return showExternalRoles;
    }

    public String getText()
    {
        return text;
    }

    public List<String> getRoleIds()
    {
        return roleIds;
    }

    public List<String> getPrivilegeIds()
    {
        return privilegeIds;
    }

    public boolean applies( RoleAndPrivilegeListResource resource )
    {
        if ( resource != null )
        {
            if ( resource.getType().equals( "role" ) )
            {
                if ( ( ( isShowRoles() && !resource.isExternal() ) || ( isShowExternalRoles() && resource.isExternal() ) )
                    && ( resource.isExternal() || getRoleIds().isEmpty() || getRoleIds().contains( resource.getId() ) )
                    && ( StringUtils.isEmpty( getText() ) || resource.getName().contains( getText() ) ) )
                {
                    return true;
                }
            }
            else if ( resource.getType().equals( "privilege" ) )
            {
                if ( isShowPrivileges()
                    && ( getPrivilegeIds().isEmpty() || getPrivilegeIds().contains( resource.getId() ) )
                    && ( StringUtils.isEmpty( getText() ) || resource.getName().contains( getText() ) ) )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
