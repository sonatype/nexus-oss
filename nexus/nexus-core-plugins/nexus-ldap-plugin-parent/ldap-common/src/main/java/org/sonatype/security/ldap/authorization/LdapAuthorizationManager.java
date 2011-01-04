/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.ldap.authorization;

import java.util.Set;
import java.util.TreeSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.authorization.AbstractReadOnlyAuthorizationManager;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.authorization.NoSuchPrivilegeException;
import org.sonatype.security.authorization.NoSuchRoleException;
import org.sonatype.security.authorization.Privilege;
import org.sonatype.security.authorization.Role;

import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.realms.LdapManager;

@Component( role = AuthorizationManager.class, hint = "LDAP" )
public class LdapAuthorizationManager
    extends AbstractReadOnlyAuthorizationManager
{

    @Requirement
    private LdapManager ldapManager;

    private Logger logger = LoggerFactory.getLogger( getClass() ); 

    public String getSource()
    {
        return "LDAP";
    }

    public Set<String> listRoleIds()
    {
        Set<String> result = null;
        try
        {
            result = ldapManager.getAllGroups();
        }
        catch ( LdapDAOException e )
        {
            this.logger.debug( "Problem getting list of LDAP Groups: " + e.getMessage(), e );
        }
        return result;
    }

    public Set<Role> listRoles()
    {
        Set<Role> result = new TreeSet<Role>();
        try
        {
            for ( String roleId : ldapManager.getAllGroups() )
            {
                Role role = new Role();
                role.setName( roleId );
                role.setRoleId( roleId );
                role.setSource( this.getSource() );
                result.add( role );
            }
        }
        catch ( LdapDAOException e )
        {
            this.logger.debug( "Problem getting list of LDAP Groups: " + e.getMessage(), e );
        }
        return result;

    }

    public Role getRole( String roleId )
        throws NoSuchRoleException
    {
        try
        {
            String roleName = this.ldapManager.getGroupName( roleId );

            if ( roleName == null )
            {
                throw new NoSuchRoleException( "Role: " + roleId + " was not found in LDAP." );
            }

            Role role = new Role();
            role.setName( roleId );
            role.setRoleId( roleId );
            role.setSource( this.getSource() );

            return role;
        }
        catch ( LdapDAOException e )
        {
            throw new NoSuchRoleException( "Role: " + roleId + " was not found in LDAP.", e );
        }
        catch ( NoSuchLdapGroupException e )
        {
            throw new NoSuchRoleException( "Role: " + roleId + " was not found in LDAP.", e );
        }
    }

    public Set<Privilege> listPrivileges()
    {
        return null;
    }

    public Privilege getPrivilege( String privilegeId )
        throws NoSuchPrivilegeException
    {
        return null;
    }

}
