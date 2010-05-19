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
package org.sonatype.security.realms;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.Sha1CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * An Authorizing Realm backed by an XML file see the security-model-xml module. This model defines users, roles, and
 * privileges. This realm ONLY handles authorization.
 * 
 * @author Brian Demers
 */
@Component( role = Realm.class, hint = XmlAuthorizingRealm.ROLE, description = "Xml Authorizing Realm" )
public class XmlAuthorizingRealm
    extends AuthorizingRealm
    implements Realm
{
    public static final String ROLE = "XmlAuthorizingRealm";

    @Requirement
    private UserManager userManager;

    public XmlAuthorizingRealm()
    {
        setCredentialsMatcher( new Sha1CredentialsMatcher() );
    }

    @Override
    public String getName()
    {
        return XmlAuthorizingRealm.class.getName();
    }

    @Override
    public boolean supports( AuthenticationToken token )
    {
        return false;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection principals )
    {
        if ( principals == null )
        {
            throw new AuthorizationException( "Cannot authorize with no principals." );
        }

        // TODO: We should check where this principal came from and only load the roles for it
        String username = principals.getPrimaryPrincipal().toString();
        Set<String> roles = new HashSet<String>();
        try
        {
            // get just the role Id from the user
            for ( RoleIdentifier roleIdentifier : userManager.getUser( username ).getRoles() )
            {
                roles.add( roleIdentifier.getRoleId() );
            }
        }
        catch ( UserNotFoundException e )
        {
            throw new AuthorizationException( "User for principals: " + principals.getPrimaryPrincipal()
                + " could not be found.", e );
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo( roles );

        return info;
    }
}
