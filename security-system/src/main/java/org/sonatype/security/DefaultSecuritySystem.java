package org.sonatype.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.mgt.RealmSecurityManager;
import org.jsecurity.realm.Realm;
import org.jsecurity.subject.PrincipalCollection;
import org.jsecurity.subject.Subject;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.authorization.AuthorizationException;
import org.sonatype.security.authorization.AuthorizationManager;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.users.User;
import org.sonatype.security.users.UserManager;
import org.sonatype.security.users.UserSearchCriteria;

/**
 * This implementation wraps a jsecurity/Ki SecurityManager, and adds user management.
 */
@Component( role = SecuritySystem.class )
public class DefaultSecuritySystem
    implements SecuritySystem, Initializable
{
    @Requirement( hint = "file" )
    private SecurityConfigurationSource configSource;

    @Requirement
    private RealmSecurityManager securityManager;

    @Requirement( role = UserManager.class )
    private Map<String, UserManager> userManagerMap;
    
    @Requirement
    private PlexusContainer container;
    
    @Requirement
    private Logger logger; 

    public Subject login( AuthenticationToken token )
        throws AuthenticationException
    {
        try
        {
            return this.securityManager.login( token );
        }
        catch ( org.jsecurity.authc.AuthenticationException e )
        {
            throw new AuthenticationException( e.getMessage(), e );
        }
    }

    public Subject getSubject()
    {
        return this.securityManager.getSubject();
    }

    public void logout( PrincipalCollection principal )
    {
        this.securityManager.logout( principal );
    }

    public boolean isPermitted( PrincipalCollection principal, String permission )
    {
        return this.securityManager.isPermitted( principal, permission );
    }

    public boolean[] isPermitted( PrincipalCollection principal, List<String> permissions )
    {
        return this.securityManager.isPermitted( principal, permissions.toArray( new String[permissions.size()] ) );
    }

    public void checkPermission( PrincipalCollection principal, String permission )
        throws AuthorizationException
    {
        try
        {
            this.securityManager.checkPermission( principal, permission );
        }
        catch ( org.jsecurity.authz.AuthorizationException e )
        {
            throw new AuthorizationException( e.getMessage(), e );
        }

    }

    public void checkPermission( PrincipalCollection principal, List<String> permissions )
        throws AuthorizationException
    {
        try
        {
            this.securityManager.checkPermissions( principal, permissions.toArray( new String[permissions.size()] ) );
        }
        catch ( org.jsecurity.authz.AuthorizationException e )
        {
            throw new AuthorizationException( e.getMessage(), e );
        }
    }

    public UserManager getUserManager( String sourceId )
    {
        return this.userManagerMap.get( sourceId );
    }

    public Set<User> searchUsers( UserSearchCriteria criteria )
    {
        // TODO: do we want to expose the search method, or just have use a new UserManager that will aggregate the
        // results (like the old PlexusUserLocator) for example the sourceId 'ALL' would return the aggregator

        return null;
    }

    public AuthorizationManager getAuthorizationManager( String sourceId )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void initialize()
        throws InitializationException
    {
        // load the configuration

        try
        {
            this.configSource.loadConfiguration();
            
            if( this.configSource.getConfiguration() == null)
            {
                throw new InitializationException( "Failed to load the security configuration." );
            }
            
            this.securityManager.setRealms( new ArrayList<Realm>( this.getRealmsFromConfigSource() ) );
        }
        catch ( Exception e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
    }
    
    private Collection<Realm> getRealmsFromConfigSource()
    {
        Set<Realm> realms = new HashSet<Realm>();

        List<String> realmIds = this.configSource.getConfiguration().getRealms();

        for ( String realmId : realmIds )
        {
            try
            {
                // First will load from plexus container
                realms.add( (Realm) container.lookup( Realm.class, realmId ) );
            }
            catch ( ComponentLookupException e )
            {
                this.logger.debug( "Failed to look up realm as plexus component, trying a Class.forName()", e );
                // If that fails, will simply use reflection to load
                try
                {
                    realms.add( (Realm) Class.forName( realmId ).newInstance() );
                }
                catch ( Exception e1 )
                {
                    this.logger.error( "Unable to lookup security realms", e );
                }
            }
        }

        return realms;
    }
    
}
