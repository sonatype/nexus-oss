/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.realms.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.configuration.validation.ValidationContext;

public class SecurityValidationContext
    implements ValidationContext
{
    private List<String> existingPrivilegeIds;

    private List<String> existingRoleIds;

    private List<String> existingUserIds;

    private Map<String, String> existingEmailMap;

    private Map<String, List<String>> roleContainmentMap;

    private Map<String, String> existingRoleNameMap;

    private Map<String, List<String>> existingUserRoleMap;

    public void addExistingPrivilegeIds()
    {
        if ( this.existingPrivilegeIds == null )
        {
            this.existingPrivilegeIds = new ArrayList<String>();
        }
    }

    public void addExistingRoleIds()
    {
        if ( this.existingRoleIds == null )
        {
            this.existingRoleIds = new ArrayList<String>();
        }

        if ( this.roleContainmentMap == null )
        {
            this.roleContainmentMap = new HashMap<String, List<String>>();
        }

        if ( this.existingRoleNameMap == null )
        {
            this.existingRoleNameMap = new HashMap<String, String>();
        }

        if ( this.existingUserRoleMap == null )
        {
            this.existingUserRoleMap = new HashMap<String, List<String>>();
        }
    }

    public void addExistingUserIds()
    {
        if ( this.existingUserIds == null )
        {
            this.existingUserIds = new ArrayList<String>();
        }

        if ( this.existingEmailMap == null )
        {
            this.existingEmailMap = new HashMap<String, String>();
        }
    }

    public List<String> getExistingPrivilegeIds()
    {
        return existingPrivilegeIds;
    }

    public List<String> getExistingRoleIds()
    {
        return existingRoleIds;
    }

    public List<String> getExistingUserIds()
    {
        return existingUserIds;
    }

    public Map<String, String> getExistingEmailMap()
    {
        return existingEmailMap;
    }

    public Map<String, List<String>> getRoleContainmentMap()
    {
        return roleContainmentMap;
    }

    public Map<String, String> getExistingRoleNameMap()
    {
        return existingRoleNameMap;
    }

    public Map<String, List<String>> getExistingUserRoleMap()
    {
        return existingUserRoleMap;
    }

}
