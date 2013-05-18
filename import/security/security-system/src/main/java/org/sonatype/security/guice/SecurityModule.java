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
package org.sonatype.security.guice;

import java.lang.reflect.Constructor;

import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.guice.ShiroModule;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

import com.google.common.base.Throwables;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * Extends ShiroModule to configure commonly set commponents such as SessionDAO, Authenticator, Authorizer, etc.
 * 
 * @since 2.7
 */
public class SecurityModule
    extends ShiroModule
{
    @Override
    protected void configureShiro()
    {
        bindRealm().to( EmptyRealm.class ); // not used in practice, just here to keep Shiro module happy

        // configure our preferred security components
        bind( SessionDAO.class ).to( EnterpriseCacheSessionDAO.class ).asEagerSingleton();
        bind( Authenticator.class ).to( FirstSuccessfulModularRealmAuthenticator.class ).in( Singleton.class );
        bind( Authorizer.class ).to( ExceptionCatchingModularRealmAuthorizer.class ).in( Singleton.class );
    }

    @Override
    protected void bindSecurityManager( AnnotatedBindingBuilder<? super SecurityManager> bind )
    {
        // prefer the default constructor; we'll set the realms programatically
        bind( DefaultSecurityManager.class ).toConstructor( ctor( DefaultSecurityManager.class ) ).asEagerSingleton();

        // bind RealmSecurityManager and SecurityManager to _same_ component
        bind( RealmSecurityManager.class ).to( DefaultSecurityManager.class );
        bind.to( DefaultSecurityManager.class );

        // bindings used by external modules
        expose( RealmSecurityManager.class );
    }

    @Override
    protected void bindSessionManager( AnnotatedBindingBuilder<SessionManager> bind )
    {
        bind.toConstructor( ctor( DefaultSessionManager.class ) ).asEagerSingleton();
    }

    /**
     * Empty {@link Realm} - only used to satisfy Shiro's need for an initial realm binding.
     */
    @Singleton
    private static final class EmptyRealm
        implements Realm
    {
        public String getName()
        {
            return getClass().getName();
        }

        public boolean supports( AuthenticationToken token )
        {
            return false;
        }

        public AuthenticationInfo getAuthenticationInfo( AuthenticationToken token )
        {
            return null;
        }
    }

    /**
     * @return Public constructor with given parameterTypes; wraps checked exceptions
     */
    private static final <T> Constructor<T> ctor( Class<T> clazz, Class<?>... parameterTypes )
    {
        try
        {
            return clazz.getConstructor( parameterTypes );
        }
        catch ( Exception e )
        {
            Throwables.propagateIfPossible( e );
            throw new ConfigurationException( e );
        }
    }
}
