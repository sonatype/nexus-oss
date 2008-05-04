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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.sonatype.nexus.proxy.LoggingComponent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.RepositoryPermission;
import org.sonatype.nexus.proxy.repository.Repository;

import com.sonatype.security.ldap.mgmt.LdapAuthConfiguration;
import com.sonatype.security.ldap.mgmt.LdapManagementException;
import com.sonatype.security.ldap.mgmt.NoSuchUserException;
import com.sonatype.security.ldap.model.LdapUser;

/**
 * The Class LdapConsumer.
 */
public class LdapConsumer
    extends LoggingComponent
{

    /** The configuration. */
    private Map<String, Object> configuration;

    /** The ldap auth configuration. */
    private LdapAuthConfiguration ldapAuthConfiguration;

    /** The ldap manager. */
    private LdapManager ldapManager;

    /** The initial dir context. */
    private InitialDirContext initialDirContext;

    public InitialDirContext getInitialDirContext()
    {
        if ( initialDirContext == null )
        {
            try
            {
                initialDirContext = getLdapManager().getInitialDirContext( configuration );
            }
            catch ( NamingException e )
            {
                getLogger().error( "Cannot get initial dir context.", e );
            }
        }
        return initialDirContext;
    }

    public void setInitialDirContext( InitialDirContext initialDirContext )
    {
        this.initialDirContext = initialDirContext;
    }

    public LdapAuthConfiguration getLdapAuthConfiguration()
    {
        return ldapAuthConfiguration;
    }

    public void setLdapAuthConfiguration( LdapAuthConfiguration ldapAuthConfiguration )
    {
        this.ldapAuthConfiguration = ldapAuthConfiguration;
    }

    public LdapManager getLdapManager()
        throws NamingException
    {
        return ldapManager;
    }

    public void setLdapManager( LdapManager ldapManager )
    {
        this.ldapManager = ldapManager;
    }

    public LdapConsumer()
    {
        super();
        this.configuration = new HashMap<String, Object>();
        this.configuration.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
        this.configuration.put( Context.SECURITY_AUTHENTICATION, "simple" );
        // this.configuration.put( "com.sun.jndi.ldap.connect.pool", Boolean.TRUE.toString() );
        this.ldapAuthConfiguration = new LdapAuthConfiguration();
    }

    /**
     * Gets the configuration.
     * 
     * @return the configuration
     */
    public Map<String, Object> getConfiguration()
    {
        return configuration;
    }

    /**
     * Sets the configuration.
     * 
     * @param configuration the configuration
     */
    public void setConfiguration( Map<String, Object> configuration )
    {
        this.configuration = configuration;
    }

    public boolean ldapAuthenticate( String username, String password )
    {
        try
        {
            try
            {
                LdapUser ldapUser = getLdapManager().getLdapUserManager().getUser(
                    username,
                    getInitialDirContext(),
                    getLdapAuthConfiguration() );
                return getLdapManager().getLdapUserManager().getPasswordEncoderManager().isPasswordValid(
                    ldapUser.getPassword(),
                    password,
                    null );
            }
            catch ( NoSuchUserException e )
            {
                getLogger().info( "User " + username + " does not exists." );
            }
            catch ( LdapManagementException e )
            {
                getLogger().error( "Got LDAP error during authentication.", e );
            }
            return false;
        }
        catch ( NamingException e )
        {
            getLogger().error( "Could not authenticate.", e );
            return false;
        }
    }

    public boolean ldapAuthorize( String username, ResourceStoreRequest request, Repository repository,
        RepositoryPermission permission )
    {
        String group = repositoryPermission2LdapGroup( permission );
        return ldapAuthorize( username, group );
    }

    public boolean ldapAuthorize( String username, String path, RepositoryPermission permission )
    {
        String group = repositoryPermission2LdapGroup( permission );
        return ldapAuthorize( username, group );
    }

    protected boolean ldapAuthorize( String username, String group )
    {
        try
        {
            try
            {
                Set<String> groups = getLdapManager().getLdapGroupManager().getGroupMembership(
                    username,
                    getInitialDirContext(),
                    getLdapAuthConfiguration() );
                return groups.contains( group );
            }
            catch ( LdapManagementException e )
            {
                getLogger().error( "Got LDAP error during authentication.", e );
            }
            return false;
        }
        catch ( NamingException e )
        {
            getLogger().error( "Could not authorize.", e );
            return false;
        }
    }

    private String repositoryPermission2LdapGroup( RepositoryPermission permission )
    {
        return permission.getId();
    }

}
