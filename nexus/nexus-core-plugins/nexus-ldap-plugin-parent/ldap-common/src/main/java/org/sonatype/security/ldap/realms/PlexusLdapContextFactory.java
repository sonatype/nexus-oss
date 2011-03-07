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
package org.sonatype.security.ldap.realms;

import java.net.MalformedURLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.tools.LdapURL;

import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

@Component( role = LdapContextFactory.class, hint = "PlexusLdapContextFactory" )
public class PlexusLdapContextFactory
    implements LdapContextFactory
{

    @Requirement
    private Logger logger;

    @Requirement
    private LdapConfiguration ldapConfig;

    /**
     * The Sun LDAP property used to enable connection pooling. This is used in the default implementation to enable
     * LDAP connection pooling.
     */
    protected static final String SUN_CONNECTION_POOLING_PROPERTY = "com.sun.jndi.ldap.connect.pool";

    protected String contextFactoryClassName = "com.sun.jndi.ldap.LdapCtxFactory";

    private boolean pooling = true;

    public LdapContext getSystemLdapContext()
        throws NamingException
    {   
        // if we do not have a connectionInfo objcet in the config we are in an unconfigured state. A naming exception
        // is the best we can do.
        if ( ldapConfig.readConnectionInfo() == null )
        {
            throw new NamingException( "LDAP Realm is not configured." );
        }
        
        return this.getLdapContext( ldapConfig.readConnectionInfo().getSystemUsername(), ldapConfig
            .readConnectionInfo().getSystemPassword(), true );
    }

    @Override
    public LdapContext getLdapContext( String username, String password )
        throws NamingException
    {
        return this.getLdapContext( username, password, false );
    }
    
    
    public LdapContext getLdapContext( Object principal, Object credentials )
        throws NamingException
    {
        return this.getLdapContext( principal.toString(), credentials.toString(), false );
    }

    private LdapContext getLdapContext( String username, String password, boolean systemLogin )
        throws NamingException
    {
        
        // if we do not have a connectionInfo objcet in the config we are in an unconfigured state. A naming exception
        // is the best we can do.
        if ( ldapConfig.readConnectionInfo() == null )
        {
            throw new NamingException( "LDAP Realm is not configured." );
        }

        CConnectionInfo connInfo = this.ldapConfig.readConnectionInfo();

        if ( connInfo.getSearchBase() == null )
        {
            throw new IllegalStateException( "A search base must be specified." );
        }

        String url;
        try
        {
            url = new LdapURL( connInfo.getProtocol(), connInfo.getHost(), connInfo.getPort(), connInfo.getSearchBase() )
                .toString();
        }
        catch ( MalformedURLException e )
        {
            // log an error, because the user could still log in and fix the config.
            this.logger.error( "LDAP Configuration is Invalid." );
            throw new NamingException( "Invalid LDAP URL: " + e.getMessage() );
        }

        if ( url == null )
        {
            throw new IllegalStateException( "An LDAP URL must be specified of the form ldap://<hostname>:<port>" );
        }

        // if (username != null && principalSuffix != null) {
        // username += principalSuffix;
        // }

        Hashtable<String, String> env = new Hashtable<String, String>();

        // if the Authentication scheme is none, and this is not the system ctx we need to set the scheme to 'simple'
        if ( "none".equals( connInfo.getAuthScheme() ) && !systemLogin )
        {
            env.put( Context.SECURITY_AUTHENTICATION, "simple" );
        }
        else
        {
            env.put( Context.SECURITY_AUTHENTICATION, connInfo.getAuthScheme() );
        }
        // username
        if ( username != null )
        {
            env.put( Context.SECURITY_PRINCIPAL, username );
        }
        // password
        if ( password != null )
        {
            env.put( Context.SECURITY_CREDENTIALS, password );
        }

        if ( connInfo.getRealm() != null )
        {
            env.put( "java.naming.security.sasl.realm", connInfo.getRealm() );
        }
        env.put( Context.INITIAL_CONTEXT_FACTORY, contextFactoryClassName );
        env.put( Context.PROVIDER_URL, url );
        // env.put(Context.REFERRAL, referral);

        // Only pool connections for system contexts
        if ( pooling && systemLogin )
        {
            // Enable connection pooling
            env.put( SUN_CONNECTION_POOLING_PROPERTY, "true" );
        }

        // if (additionalEnvironment != null) {
        // env.putAll(additionalEnvironment);
        // }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Initializing LDAP context using URL [" + url + "] and username [" + username + "] "
                + "with pooling [" + ( pooling ? "enabled" : "disabled" ) + "]" );
        }

        return new InitialLdapContext( env, null );
    }

    /**
     * @param ldapConfig the ldapConfig to set
     */
    public void setLdapConfiguration( LdapConfiguration ldapConfig )
    {
        this.ldapConfig = ldapConfig;
    }
}
