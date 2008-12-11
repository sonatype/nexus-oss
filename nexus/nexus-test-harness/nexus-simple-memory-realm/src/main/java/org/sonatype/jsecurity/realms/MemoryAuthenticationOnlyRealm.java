/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.jsecurity.realms;

import java.util.HashMap;
import java.util.Map;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.SimpleCredentialsMatcher;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

/**
 * This is a sample of how you can inject your own authentication system
 * but still leave the authorization to nexus.
 * 
 * This MemoryAuthenticationOnlyRealm will handle authentication on its own
 * 
 * This class can also be loaded by nexus as either a regular class, or as a 
 * plexus component.  In the nexus.xml file, you can remove the default
 * XmlAuthenticatingRealm and add this realm as either 
 * org.sonatype.jsecurity.realms.MemoryAuthenticationOnlyRealm, or
 * use the role-hint of MemoryAuthenticationOnlyRealm.
 * 
 * @plexus.component role="org.jsecurity.realm.Realm" role-hint="MemoryAuthenticationOnlyRealm"
 */
public class MemoryAuthenticationOnlyRealm
    extends AuthorizingRealm
{
    // Map containing username/password pairs
    private Map<String,String> authenticationMap = new HashMap<String,String>();
        
    /**
     * This is where we are building the security model, not that the passwords have
     * been changed from the default nexus security, to make for easy validation
     */
    public MemoryAuthenticationOnlyRealm()
    {
        // As this is a simple test realm, only using simple credentials
        // just a string compare, no hashing involved
        setCredentialsMatcher( new SimpleCredentialsMatcher() );
        
        authenticationMap.put( "admin", "admin321" );
        authenticationMap.put( "deployment", "deployment321" );
        authenticationMap.put( "anonymous", "anonymous" );
    }

    public String getName()
    {
        return MemoryAuthenticationOnlyRealm.class.getName();
    }
    
    /**
     * This method is where the authentication is controlled.  You will receive a
     * token, from which you can retrieve the username.  Then you can lookup in your
     * storage, the credentials for that user, place those in an AuthenticationInfo
     * object and return it, the credential matcher will handle comparing them.
     * 
     * @see org.jsecurity.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.jsecurity.authc.AuthenticationToken)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken arg0 )
        throws AuthenticationException
    {
        if ( !UsernamePasswordToken.class.isAssignableFrom( arg0.getClass() ) )
        {
            return null;
        }
        
        String username = ( ( UsernamePasswordToken ) arg0 ).getUsername();
        
        String password = authenticationMap.get( username );
        
        if ( password == null )
        {
            throw new AuthenticationException( "Invalid username '" + username + "'");
        }
        
        return new SimpleAuthenticationInfo( username, password, getName() );
    }
    
    /**
     * As this is an authentication only realm, we just return null for authorization
     * 
     * @see org.jsecurity.realm.AuthorizingRealm#doGetAuthorizationInfo(org.jsecurity.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection arg0 )
    {
        return null;
    }
}
