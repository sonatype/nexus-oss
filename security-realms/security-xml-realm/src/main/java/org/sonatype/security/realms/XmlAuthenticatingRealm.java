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
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Description;
import org.sonatype.security.model.CUser;
import org.sonatype.security.realms.tools.ConfigurationManager;
import org.sonatype.security.realms.tools.ConfigurationManagerAction;
import org.sonatype.security.usermanagement.UserNotFoundException;

import com.google.common.base.Throwables;

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
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    
    public static final String ROLE = "XmlAuthenticatingRealm";
    
    private ConfigurationManager configuration;
    
    private PasswordService passwordService;
    
    private final int MAX_LEGACY_PASSWORD_LENGTH = 40;

    @Inject
	public XmlAuthenticatingRealm( @Named( "default" ) ConfigurationManager configuration,
                                   PasswordService passwordService)
    {
        this.configuration = configuration;
        this.passwordService = passwordService;
        
        PasswordMatcher passwordMatcher = new PasswordMatcher();
        passwordMatcher.setPasswordService(this.passwordService);
        setCredentialsMatcher( passwordMatcher );
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
        	//Check for legacy user that has unsalted password hash
        	//Update if legacy user, and valid credentials were specified
        	if(this.isLegacyUser(user) && this.isValidCredentials(upToken, user))
        	{
        		this.reHashPassword(user, new String(upToken.getPassword()));
        	}
        	
        	return this.createAuthenticationInfo(user);
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
    
    
    /*
     * Re-hash user password, and persist changes
     * 
     * @param user to update
     * @param password cleartext password to hash
     */
    private void reHashPassword(final CUser user, final String password)    	
    {
        //Store current values to rollback if update fails
        final String currentPasswordHash = user.getPassword();
        
        try
        {
            configuration.runWrite(new ConfigurationManagerAction()
            {
                @Override
                public void run()
                    throws Exception
                {
                    try
                    {
                        user.setPassword(passwordService.encryptPassword(password));
                        configuration.updateUser(user);
                        configuration.save();
                    }
                    catch(Exception e)
                    {
                        //Update failed, rollback to previous values
                        user.setPassword(currentPasswordHash);
                        logger.error("Unable to update hash for user {}", user.getId());
                    }
                }
            });
        }
        catch(Exception e)
        {
            throw Throwables.propagate(e);
        }
    }
    
    /*
     * Checks to see if the credentials in token match the credentials stored on
     * user
     * 
     * @param token the username/password token containing the credentials to
     * verify
     * @param the user object containing the stored credentials
     * @return true if credentials match, false otherwise
     */
    private boolean isValidCredentials(UsernamePasswordToken token, CUser user)
    {
        boolean credentialsValid = false;
        
        AuthenticationInfo info = this.createAuthenticationInfo(user);
        CredentialsMatcher matcher = this.getCredentialsMatcher();
        if(matcher != null)
        {
            if(matcher.doCredentialsMatch(token, info))
            {
                credentialsValid = true;
            }
        }
        
        return credentialsValid;
    }
    
    /*
     * Checks to see if the specified user is a legacy user
     * A legacy user has an unsalted password
     * 
     * @param user to check
     * @return true if legacy user, false otherwise
     */
    private boolean isLegacyUser(CUser user)
    {
        //Legacy users have a shorter, unsalted, SHA1 or MD5 based hash
        return user.getPassword().length() <= this.MAX_LEGACY_PASSWORD_LENGTH;
    }
    
    /*
     * Creates an authentication info object
     * using the credentials of the provided user
     * 
     * @param user
     * @return authentication info object based on user credentials
     */
    private AuthenticationInfo createAuthenticationInfo(CUser user)
    {
    	return new SimpleAuthenticationInfo( user.getId(), user.getPassword().toCharArray(), getName() );
    }
}
