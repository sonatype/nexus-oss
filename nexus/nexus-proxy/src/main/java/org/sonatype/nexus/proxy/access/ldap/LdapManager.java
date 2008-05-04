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
package org.sonatype.nexus.proxy.access.ldap;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import com.sonatype.security.ldap.mgmt.LdapGroupManager;
import com.sonatype.security.ldap.mgmt.LdapUserManager;

/**
 * The Interface LdapManager.
 */
public interface LdapManager
{

    String ROLE = LdapManager.class.getName();

    InitialDirContext getInitialDirContext( Map<String, Object> env )
        throws NamingException;

    LdapUserManager getLdapUserManager();

    LdapGroupManager getLdapGroupManager();

}
