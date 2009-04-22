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
package org.sonatype.security.realms.tools;

import java.util.List;

import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;

/**
 * This class should really be doing an uber generic clone, but it is this for now
 */
public class ObjectCloner
{
    @SuppressWarnings("unchecked")
    protected static final CUser clone( CUser user )
    {
        if ( user == null )
        {
            return null;
        }
        
        CUser cloned = new CUser();
        
        cloned.setEmail( user.getEmail() );
        cloned.setName( user.getName() );
        cloned.setPassword( user.getPassword() );
        cloned.setStatus( user.getStatus() );
        cloned.setId( user.getId() );
     
//        if ( user.getRoles() != null )
//        {
//            for ( String roleId : ( List<String> ) user.getRoles() )
//            {
//                cloned.addRole( roleId );
//            }
//        }
        
        return cloned;
    }
    
    @SuppressWarnings("unchecked")
    protected static final CRole clone( CRole role )
    {
        if ( role == null )
        {
            return null;
        }
        
        CRole cloned = new CRole();
        
        cloned.setDescription( role.getDescription() );
        cloned.setId( role.getId() );
        cloned.setName( role.getName() );
        cloned.setSessionTimeout( role.getSessionTimeout() );
        
        if ( role.getRoles() != null )
        {
            for ( String roleId : ( List<String> ) role.getRoles() )
            {
                cloned.addRole( roleId );
            }
        }
        
        if ( role.getPrivileges() != null )
        {
            for ( String privilegeId : ( List<String> ) role.getPrivileges() )
            {
                cloned.addPrivilege( privilegeId );
            }
        }
        
        return cloned;
    }
    
    @SuppressWarnings("unchecked")
    protected static final CPrivilege clone( CPrivilege privilege )
    {
        if ( privilege == null )
        {
            return privilege;
        }
        
        CPrivilege cloned = new CPrivilege();
        
        cloned.setDescription( privilege.getDescription() );
        cloned.setId( privilege.getId() );
        cloned.setName( privilege.getName() );
        cloned.setType( privilege.getType() );
        
        if ( privilege.getProperties() != null )
        {
            for ( CProperty prop : ( List<CProperty> ) privilege.getProperties() )
            {
                cloned.addProperty( clone( prop ) );
            }
        }
        
        return cloned;
    }
    
    protected static final CProperty clone( CProperty property )
    {
        if ( property == null )
        {
            return null;
        }
        
        CProperty cloned = new CProperty();
        
        cloned.setKey( property.getKey() );
        cloned.setValue( property.getValue() );
        
        return cloned;
    }
}
