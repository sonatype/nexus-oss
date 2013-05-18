/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
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
package org.sonatype.security;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.util.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.inject.Nullable;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

/**
 * Componentize the Shiro DefaultSecurityManager, and sets up caching.
 * 
 * @author Brian Demers
 * @deprecated use shiro-guice with @{link org.sonatype.security.guice.SecurityModule} instead.
 */
@Singleton
@Typed( RealmSecurityManager.class )
@Named( "default" )
@Deprecated
public class DefaultRealmSecurityManager
    extends DefaultSecurityManager
    implements Initializable
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private RolePermissionResolver rolePermissionResolver;

    @Inject
    public DefaultRealmSecurityManager( @Nullable RolePermissionResolver rolePermissionResolver )
    {
        logger.info( "@Deprecated use shiro-guice with org.sonatype.security.guice.SecurityModule instead" );

        this.rolePermissionResolver = rolePermissionResolver;
        init();
    }

    public void init()
        throws ShiroException
    {
        this.setSessionManager( new DefaultSessionManager() );

        // This could be injected
        // Authorizer
        ExceptionCatchingModularRealmAuthorizer authorizer =
            new ExceptionCatchingModularRealmAuthorizer( this.getRealms() );

        // if we have a Role Permission Resolver, set it, if not, don't worry about it
        if ( rolePermissionResolver != null )
        {
            authorizer.setRolePermissionResolver( rolePermissionResolver );
            logger.debug( "RolePermissionResolver was set to " + authorizer.getRolePermissionResolver() );
        }
        else
        {
            logger.warn( "No RolePermissionResolver is set" );
        }
        this.setAuthorizer( authorizer );

        // set the realm authenticator, that will automatically deligate the authentication to all the realms.
        FirstSuccessfulModularRealmAuthenticator realmAuthenticator = new FirstSuccessfulModularRealmAuthenticator();
        realmAuthenticator.setAuthenticationStrategy( new FirstSuccessfulStrategy() );

        // Authenticator
        this.setAuthenticator( realmAuthenticator );
    }
}
