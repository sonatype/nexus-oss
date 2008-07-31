/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security;

import java.io.IOException;
import java.util.Collection;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;

public interface MutableNexusSecurityConfiguration
{
    // Users
    Collection<CUser> listUsers();
    
    void createUser( CUser settings )
        throws ConfigurationException,
            IOException;

    CUser readUser( String id )
        throws NoSuchUserException;

    void updateUser( CUser settings )
        throws ConfigurationException,
            NoSuchUserException,
            IOException;

    void deleteUser( String id )
        throws IOException,
            NoSuchUserException;
    
    void resetPassword( String id )
        throws IOException,
            NoSuchUserException;
    
    void forgotUserId( String email )
        throws IOException,
            NoSuchUserException;
    
    void forgotPassword( String userId, String email )
        throws IOException,
            NoSuchUserException;

    //Roles
    Collection<CRole> listRoles();
    
    void createRole( CRole settings )
        throws ConfigurationException,
            IOException;

    CRole readRole( String id )
        throws NoSuchRoleException;

    void updateRole( CRole settings )
        throws ConfigurationException,
            NoSuchRoleException,
            IOException;

    void deleteRole( String id )
        throws IOException,
            NoSuchRoleException;

    //Application privileges
    Collection<CApplicationPrivilege> listApplicationPrivileges();
    
    void createApplicationPrivilege( CApplicationPrivilege settings )
        throws ConfigurationException,
            IOException;

    CApplicationPrivilege readApplicationPrivilege( String id )
        throws NoSuchPrivilegeException;

    void updateApplicationPrivilege( CApplicationPrivilege settings )
        throws ConfigurationException,
            NoSuchPrivilegeException,
            IOException;

    void deleteApplicationPrivilege( String id )
        throws IOException,
            NoSuchPrivilegeException;

    //Repository Target privileges
    Collection<CRepoTargetPrivilege> listRepoTargetPrivileges();
    
    void createRepoTargetPrivilege( CRepoTargetPrivilege settings )
        throws ConfigurationException,
            IOException;

    CRepoTargetPrivilege readRepoTargetPrivilege( String id )
        throws NoSuchPrivilegeException;

    void updateRepoTargetPrivilege( CRepoTargetPrivilege settings )
        throws ConfigurationException,
            NoSuchPrivilegeException,
            IOException;

    void deleteRepoTargetPrivilege( String id )
        throws IOException,
            NoSuchPrivilegeException;
}
