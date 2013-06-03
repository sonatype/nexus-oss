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
package org.sonatype.security.web;

import java.util.Map;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

/**
 * A Web implementation of the jsecurity SecurityManager. TODO: This duplicates what the DefaultRealmSecurityManager
 * does, because the have different parents. We should look into a better way of doing this. Something like pushing the
 * configuration into the SecuritySystem. The downside to that is we would need to expose an accessor for it. ( This
 * component is loaded from a servelet ), but that might be cleaner then what we are doing now.
 * 
 * @deprecated use shiro-guice with @{link org.sonatype.security.web.guice.SecurityWebModule} instead.
 */
@Singleton
@Typed( RealmSecurityManager.class )
@Named( "web" )
@Deprecated
public class WebRealmSecurityManager
    extends DefaultWebSecurityManager
    implements org.apache.shiro.util.Initializable
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private Map<String, RolePermissionResolver> rolePermissionResolverMap;

    @Inject
    public WebRealmSecurityManager( Map<String, RolePermissionResolver> rolePermissionResolverMap )
    {
        logger.info( "@Deprecated use shiro-guice with org.sonatype.security.web.guice.SecurityWebModule instead" );

        this.rolePermissionResolverMap = rolePermissionResolverMap;

        // set the realm authenticator, that will automatically deligate the authentication to all the realms.
        FirstSuccessfulModularRealmAuthenticator realmAuthenticator = new FirstSuccessfulModularRealmAuthenticator();
        realmAuthenticator.setAuthenticationStrategy( new FirstSuccessfulStrategy() );

        // Authenticator
        this.setAuthenticator( realmAuthenticator );

        initialize();
    }

    public void initialize()
    {
        // This could be injected
        // Authorizer
        ExceptionCatchingModularRealmAuthorizer authorizer =
            new ExceptionCatchingModularRealmAuthorizer( this.getRealms() );

        // if we have a Role Permission Resolver, set it, if not, don't worry about it
        if ( !rolePermissionResolverMap.isEmpty() )
        {
            if ( rolePermissionResolverMap.containsKey( "default" ) )
            {
                authorizer.setRolePermissionResolver( rolePermissionResolverMap.get( "default" ) );
            }
            else
            {
                authorizer.setRolePermissionResolver( rolePermissionResolverMap.values().iterator().next() );
            }
            logger.debug( "RolePermissionResolver was set to " + authorizer.getRolePermissionResolver() );
        }
        else
        {
            logger.warn( "No RolePermissionResolver is set" );
        }
        this.setAuthorizer( authorizer );
    }

    public void init()
        throws ShiroException
    {
        // use cacheing for the sessions, we can tune this with a props file per application if needed
        DefaultWebSessionManager webSessionManager = new DefaultWebSessionManager();
        webSessionManager.setSessionDAO( new EnterpriseCacheSessionDAO() );
        this.setSessionManager( webSessionManager );
    }
}
