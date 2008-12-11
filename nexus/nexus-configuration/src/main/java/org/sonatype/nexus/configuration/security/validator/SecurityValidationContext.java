/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.security.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.validator.AbstractValidationContext;


public class SecurityValidationContext extends AbstractValidationContext
{
    private List<String> existingPrivilegeIds;
    
    private List<String> existingRoleIds;
    
    private List<String> existingUserIds;
    
    private Map<String,String> existingEmailMap;
    
    private Map<String,List<String>> roleContainmentMap;
    
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
            this.roleContainmentMap = new HashMap<String,List<String>>();
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
            this.existingEmailMap = new HashMap<String,String>();
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
    
    public Map<String,String> getExistingEmailMap()
    {
        return existingEmailMap;
    }
    
    public Map<String,List<String>> getRoleContainmentMap()
    {
        return roleContainmentMap;
    }
}
