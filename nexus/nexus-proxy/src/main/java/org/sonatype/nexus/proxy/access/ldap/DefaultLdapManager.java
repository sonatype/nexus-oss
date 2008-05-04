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

import java.util.Hashtable;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.InitialLdapContext;

import org.codehaus.plexus.logging.AbstractLogEnabled;

import com.sonatype.security.ldap.mgmt.LdapGroupManager;
import com.sonatype.security.ldap.mgmt.LdapUserManager;

/**
 * The Class DefaultLdapManager.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultLdapManager
    extends AbstractLogEnabled
    implements LdapManager
{

    /**
     * The group manager.
     * 
     * @plexus.requirement
     */
    private LdapGroupManager ldapGroupManager;

    /**
     * The user manager.
     * 
     * @plexus.requirement
     */
    private LdapUserManager ldapUserManager;
    
    /*
     * (non-Javadoc)
     * 
     * @see com.sonatype.security.ldap.LdapManager#getInitialDirContext()
     */
    public InitialDirContext getInitialDirContext( Map<String, Object> env )
        throws NamingException
    {
        return new InitialLdapContext( new Hashtable<String, Object>( env ), null );
    }

    public LdapGroupManager getLdapGroupManager()
    {
        return ldapGroupManager;
    }

    public LdapUserManager getLdapUserManager()
    {
        return ldapUserManager;
    }

}
