/**
// * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
import org.jsecurity.realm.ldap.LdapContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.LdapAuthenticator;
import org.sonatype.security.ldap.dao.LdapAuthConfiguration;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapGroupDAO;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.LdapUserDAO;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.DefaultLdapContextFactory;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.connector.DefaultLdapConnector;
import org.sonatype.security.ldap.realms.connector.LdapConnector;
import org.sonatype.security.ldap.realms.persist.LdapClearCacheEvent;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import org.sonatype.security.ldap.realms.tools.LdapURL;

@Component( role = LdapManager.class )
public class DefaultLdapManager
    implements LdapManager, EventListener, Initializable, Disposable
{

    private Logger logger = LoggerFactory.getLogger( getClass() );

    @Requirement
    private LdapAuthenticator ldapAuthenticator;

    @Requirement
    private LdapUserDAO ldapUserManager;

    @Requirement
    private LdapGroupDAO ldapGroupManager;

    @Requirement
    private LdapConfiguration ldapConfiguration;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    private LdapConnector ldapConnector;

    public SortedSet<String> getAllGroups()
        throws LdapDAOException
    {
        return this.getLdapConnector().getAllGroups();
    }

    public SortedSet<LdapUser> getAllUsers()
        throws LdapDAOException
    {
        return this.getLdapConnector().getAllUsers();
    }

    public String getGroupName( String groupId )
        throws LdapDAOException,
            NoSuchLdapGroupException
    {
        return this.getLdapConnector().getGroupName( groupId );
    }

    public LdapUser getUser( String username )
        throws NoSuchLdapUserException,
            LdapDAOException
    {
        return this.getLdapConnector().getUser( username );
    }

    public Set<String> getUserRoles( String userId )
        throws LdapDAOException,
            NoLdapUserRolesFoundException
    {
        return this.getLdapConnector().getUserRoles( userId );
    }

    public SortedSet<LdapUser> getUsers( int userCount )
        throws LdapDAOException
    {
        return this.getLdapConnector().getUsers( userCount );
    }

    public SortedSet<LdapUser> searchUsers( String username )
        throws LdapDAOException
    {
        return this.getLdapConnector().searchUsers( username );
    }

    private LdapConnector getLdapConnector()
        throws LdapDAOException
    {
        if ( this.ldapConnector == null )
        {
            this.ldapConnector = new DefaultLdapConnector(
                "default",
                this.ldapUserManager,
                this.ldapGroupManager,
                this.getLdapContextFactory(),
                this.getLdapAuthConfiguration() );
        }
        return this.ldapConnector;
    }

    protected LdapConfiguration getLdapConfiguration()
    {
        return this.ldapConfiguration;
    }

    protected LdapAuthConfiguration getLdapAuthConfiguration()
    {
        return this.getLdapConfiguration().getLdapAuthConfiguration();
    }

    protected LdapContextFactory getLdapContextFactory()
        throws LdapDAOException
    {
        DefaultLdapContextFactory defaultLdapContextFactory = new DefaultLdapContextFactory();

        if ( this.getLdapConfiguration() == null || this.getLdapConfiguration().readConnectionInfo() == null )
        {
            throw new LdapDAOException( "Ldap connection is not configured." );
        }

        CConnectionInfo connInfo = this.getLdapConfiguration().readConnectionInfo();

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
            throw new LdapDAOException( "Invalid LDAP URL: " + e.getMessage() );
        }

        defaultLdapContextFactory.setUsePooling( true );
        defaultLdapContextFactory.setUrl( url );
        defaultLdapContextFactory.setSystemUsername( connInfo.getSystemUsername() );
        defaultLdapContextFactory.setSystemPassword( connInfo.getSystemPassword() );
        defaultLdapContextFactory.setSearchBase( connInfo.getSearchBase() );
        defaultLdapContextFactory.setAuthentication( connInfo.getAuthScheme() );
        
        Map<String, String> connectionProperties = new HashMap<String, String>();
        // set the realm
        if ( connInfo.getRealm() != null )
        {
            connectionProperties.put( "java.naming.security.sasl.realm", connInfo.getRealm() );
        }
        defaultLdapContextFactory.setAdditionalEnvironment( connectionProperties );

        return defaultLdapContextFactory;
    }

    public LdapUser authenticateUser( String userId, String password ) throws AuthenticationException
    {
        try
        {
            LdapUser ldapUser = this.getUser( userId );

            String authScheme = this.getLdapConfiguration().readConnectionInfo().getAuthScheme();

            if ( StringUtils.isEmpty( this
                .getLdapConfiguration().readUserAndGroupConfiguration().getUserPasswordAttribute() ) )
            {
                // auth with bind

                this.ldapAuthenticator.authenticateUserWithBind(
                    ldapUser,
                    password,
                    this.getLdapContextFactory(),
                    authScheme );
            }
            else
            {
                // auth by checking password,
                this.ldapAuthenticator.authenticateUserWithPassword( ldapUser, password );
            }
            
            // everything was successful  
            return ldapUser;
        }
        catch ( Exception e )
        {
            if( this.logger.isDebugEnabled())
            {
                this.logger.debug( "Failed to find user: " + userId, e );
            }
        }
        throw new AuthenticationException( "User: " + userId + " could not be authenticated." );
    }
    
    public void onEvent( Event<?> evt )
    {
        if ( evt instanceof LdapClearCacheEvent )
        {
            // clear the connectors
            this.ldapConnector = null;
        }
    }

    public void initialize()
        throws InitializationException
    {
        this.applicationEventMulticaster.addEventListener( this );
    }

    public void dispose()
    {
        this.applicationEventMulticaster.removeEventListener( this );
    }

}
