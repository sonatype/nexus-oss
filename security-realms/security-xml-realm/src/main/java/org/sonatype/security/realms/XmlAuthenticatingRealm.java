/*
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.sonatype.inject.Description;
import org.sonatype.security.configuration.SecurityConfigurationManager;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.Sha512ThenSha1ThenMd5CredentialsMatcher;
import org.sonatype.security.usermanagement.UserNotFoundException;

/**
 * An Authentication Realm backed by an XML file see the security-model-xml module. This model defines users, roles, and
 * privileges. This realm ONLY handles authentication.
 * 
 * @author Brian Demers
 */
@Singleton
@Typed( Realm.class )
@Named( XmlAuthenticatingRealm.ROLE )
@Description( "Xml Authenticating Realm" )
public class XmlAuthenticatingRealm
    extends AuthorizingRealm
    implements Realm
{
    public static final String ROLE = "XmlAuthenticatingRealm";

    private ConfigurationManager configuration;
    
    private SecurityConfigurationManager securityConfiguration;

    @Inject
    public XmlAuthenticatingRealm( @Named( "resourceMerging" ) ConfigurationManager configuration,
    							   SecurityConfigurationManager securityConfiguration)
    {
        this.configuration = configuration;
        this.securityConfiguration = securityConfiguration;
        setCredentialsMatcher( new Sha512ThenSha1ThenMd5CredentialsMatcher(this.securityConfiguration.getHashIterations()) );
    }

    @Override
    public String getName()
    {
        return ROLE;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( AuthenticationToken token )
        throws AuthenticationException
    {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        CUser user;
        try
        {
            user = configuration.readUser( upToken.getUsername() );
        }
        catch ( UserNotFoundException e )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' cannot be retrieved.", e );
        }

        if ( user.getPassword() == null )
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' has no password, cannot authenticate." );
        }
        
        if ( CUser.STATUS_ACTIVE.equals( user.getStatus() ) )
        {
            return new SimpleAuthenticationInfo( upToken.getUsername(), user.getPassword().toCharArray(), ByteSource.Util.bytes(user.getSalt() != null ? user.getSalt() : ""), getName() );        	
        }
        else if ( CUser.STATUS_DISABLED.equals( user.getStatus() ) )
        {
            throw new DisabledAccountException( "User '" + upToken.getUsername() + "' is disabled." );
        }
        else
        {
            throw new AccountException( "User '" + upToken.getUsername() + "' is in illegal status '"
                + user.getStatus() + "'." );
        }
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( PrincipalCollection arg0 )
    {
        return null;
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configuration;
    }
}
