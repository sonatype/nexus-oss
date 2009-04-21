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
package org.sonatype.jsecurity.realms.tools.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.jsecurity.model.CUser;

public class SecurityUser
    extends CUser
        implements SecurityItem
{
    boolean readOnly;
    
    private Set<String> roles = new HashSet<String>();
    
    public SecurityUser()
    {
    }
    
    public SecurityUser( CUser user )
    {
        this( user, false );
    }
    
    public SecurityUser( CUser user, boolean readOnly )
    {
        this( user, false, null );
    }
    
    public SecurityUser( CUser user, boolean readOnly, List<String> roles )
    {
        setEmail( user.getEmail() );
        setName( user.getName() );
        setPassword( user.getPassword() );
        setStatus( user.getStatus() );
        setId( user.getId() );
        setReadOnly( readOnly );
     
        if ( roles != null )
        {
            for ( String roleId : roles )
            {
                addRole( roleId );
            }
        }
    }
    
    public Set<String> getRoles()
    {
        return roles;
    }

    public void addRole( String roleId)
    {
        this.roles.add( roleId );
    }
    
    public void setRoles( Set<String> roles )
    {
        this.roles = roles;
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