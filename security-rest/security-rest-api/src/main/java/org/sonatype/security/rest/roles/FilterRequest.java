/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.rest.roles;

import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.rest.model.RoleAndPrivilegeListFilterResourceRequest;
import org.sonatype.security.rest.model.RoleAndPrivilegeListResource;

public class FilterRequest
{
    private final boolean showPrivileges;

    private final boolean showRoles;

    private final boolean showExternalRoles;

    private final boolean onlySelected;

    private final String text;

    private final List<String> roleIds;

    private final List<String> privilegeIds;

    private final List<String> hiddenRoleIds;

    private final List<String> hiddenPrivilegeIds;

    private final String userId;

    public FilterRequest( RoleAndPrivilegeListFilterResourceRequest request )
    {
        this.showPrivileges = !request.getData().isNoPrivileges();
        this.showRoles = !request.getData().isNoRoles();
        this.showExternalRoles = !request.getData().isNoExternalRoles();
        this.onlySelected = request.getData().isOnlySelected();
        this.text = request.getData().getName();
        this.roleIds = request.getData().getSelectedRoleIds();
        this.privilegeIds = request.getData().getSelectedPrivilegeIds();
        this.hiddenRoleIds = request.getData().getHiddenRoleIds();
        this.hiddenPrivilegeIds = request.getData().getHiddenPrivilegeIds();
        this.userId = request.getData().getUserId();
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

    public boolean isOnlySelected()
    {
        return onlySelected;
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

    public List<String> getHiddenRoleIds()
    {
        return hiddenRoleIds;
    }

    public List<String> getHiddenPrivilegeIds()
    {
        return hiddenPrivilegeIds;
    }

    public String getUserId()
    {
        return userId;
    }

    public boolean applies( RoleAndPrivilegeListResource resource )
    {
        if ( resource != null )
        {
            if ( resource.getType().equals( "role" ) )
            {
                if ( ( ( isShowRoles() && !resource.isExternal() && !( getUserId() != null && getRoleIds().isEmpty() ) ) || ( isShowExternalRoles() && resource.isExternal() ) )
                    && ( !getHiddenRoleIds().contains( resource.getId() ) )
                    && ( resource.isExternal() || ( ( ( getRoleIds().isEmpty() && !isOnlySelected() ) || getRoleIds().contains(
                        resource.getId() ) ) ) )
                    && ( StringUtils.isEmpty( getText() ) || Pattern.compile( Pattern.quote( getText() ), Pattern.CASE_INSENSITIVE ).matcher( resource.getName() ).find() ) )
                {
                    return true;
                }
            }
            else if ( resource.getType().equals( "privilege" ) )
            {
                if ( isShowPrivileges()
                    && ( !getHiddenPrivilegeIds().contains( resource.getId() ) )
                    && ( ( getPrivilegeIds().isEmpty() && !isOnlySelected() ) || getPrivilegeIds().contains(
                        resource.getId() ) )
                    && ( StringUtils.isEmpty( getText() ) || Pattern.compile( Pattern.quote( getText() ), Pattern.CASE_INSENSITIVE ).matcher( resource.getName() ).find() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
