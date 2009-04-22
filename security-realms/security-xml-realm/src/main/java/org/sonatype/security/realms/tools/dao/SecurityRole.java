/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security.realms.tools.dao;

import java.util.List;

import org.sonatype.security.model.CRole;

public class SecurityRole
    extends CRole
        implements SecurityItem
{
    boolean readOnly;
    
    public SecurityRole()
    {
    }
    
    public SecurityRole( CRole role )
    {
        this( role, false );
    }
    
    public SecurityRole( CRole role, boolean readOnly )
    {
        setDescription( role.getDescription() );
        setId( role.getId() );
        setName( role.getName() );
        setSessionTimeout( role.getSessionTimeout() );
        setReadOnly( readOnly );
        
        if ( role.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) role.getRoles() )
            {
                addRole( roleId );
            }
        }
        
        if ( role.getPrivileges() != null )
        {
            for ( String privilegeId : ( List<String> ) role.getPrivileges() )
            {
                addPrivilege( privilegeId );
            }
        }
    }
    
    public boolean isReadOnly()
    {
        return readOnly;
    }
    
    public void setReadOnly( boolean readOnly )
    {
        this.readOnly = readOnly;
    }
}