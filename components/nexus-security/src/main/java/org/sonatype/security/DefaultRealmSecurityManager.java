/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
