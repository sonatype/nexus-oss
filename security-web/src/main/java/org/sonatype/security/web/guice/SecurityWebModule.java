/**
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
package org.sonatype.security.web.guice;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authz.Authorizer;
import org.apache.shiro.config.ConfigurationException;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;

import com.google.common.base.Throwables;
import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.name.Names;

/**
 * Extends ShiroWebModule to configure commonly set commponents such as SessionDAO, Authenticator, Authorizer, etc.
 * <p>
 * When {@link #injectFilterMap} is {@code true} the {@link #addFilterChain} method has no affect; instead all named
 * filters bound in this application are injected into the {@link FilterChainManager} so they can be added to filter
 * chains programatically.
 * 
 * @since 2.7
 */
public class SecurityWebModule
    extends ShiroWebModule
{
    private final boolean injectFilterMap;

    public SecurityWebModule( ServletContext servletContext, boolean injectFilterMap )
    {
        super( servletContext );
        this.injectFilterMap = injectFilterMap;
    }

    @Override
    protected void configureShiroWeb()
    {
        bindRealm().to( EmptyRealm.class ); // not used in practice, just here to keep Shiro module happy

        // configure our preferred security components
        bind( SessionDAO.class ).to( EnterpriseCacheSessionDAO.class ).asEagerSingleton();
        bind( Authenticator.class ).to( FirstSuccessfulModularRealmAuthenticator.class ).in( Singleton.class );
        bind( Authorizer.class ).to( ExceptionCatchingModularRealmAuthorizer.class ).in( Singleton.class );

        if ( injectFilterMap )
        {
            // override the default resolver with one backed by a FilterChainManager using an injected filter map
            bind( FilterChainResolver.class ).toConstructor( ctor( PathMatchingFilterChainResolver.class ) ).asEagerSingleton();
            bind( FilterChainManager.class ).toProvider( FilterChainManagerProvider.class ).in( Singleton.class );
        }

        // expose bindings to other modules
        expose( FilterChainResolver.class );
        expose( FilterChainManager.class );
    }

    @Override
    protected void bindWebSecurityManager( AnnotatedBindingBuilder<? super WebSecurityManager> bind )
    {
        // prefer the default constructor; we'll set the realms programatically
        bind( DefaultWebSecurityManager.class ).toConstructor( ctor( DefaultWebSecurityManager.class ) ).asEagerSingleton();

        // bind RealmSecurityManager and WebSecurityManager to _same_ component
        bind( RealmSecurityManager.class ).to( DefaultWebSecurityManager.class );
        bind.to( DefaultWebSecurityManager.class );

        // expose bindings to other modules
        expose( RealmSecurityManager.class );
        expose( WebSecurityManager.class );
    }

    @Override
    protected void bindSessionManager( AnnotatedBindingBuilder<SessionManager> bind )
    {
        // use native web session management instead of delegating to servlet container
        bind.toConstructor( ctor( DefaultWebSessionManager.class ) ).asEagerSingleton();
    }

    /**
     * Binds this {@link Filter} instance under the given name in the injected filter map.
     * 
     * @param name The filter name
     * @param filter The filter instance
     */
    protected void bindNamedFilter( String name, Filter filter )
    {
        Key<Filter> key = Key.get( Filter.class, Names.named( name ) );
        bind( key ).toInstance( filter );

        expose( key ); // expose binding so it appears in the aggregate injected map
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

    /**
     * Constructs a {@link DefaultFilterChainManager} from an injected {@link Filter} map.
     */
    private static final class FilterChainManagerProvider
        implements Provider<FilterChainManager>
    {
        private final FilterConfig filterConfig;

        private final Map<String, Filter> filterMap;

        @Inject
        private FilterChainManagerProvider( @Named( "SHIRO" ) ServletContext servletContext,
                                            Map<String, Filter> filterMap )
        {
            // simple configuration so we can initialize filters as we add them
            this.filterConfig = new SimpleFilterConfig( "SHIRO", servletContext );
            this.filterMap = filterMap;
        }

        public FilterChainManager get()
        {
            FilterChainManager filterChainManager = new DefaultFilterChainManager( filterConfig );
            for ( Entry<String, Filter> entry : filterMap.entrySet() )
            {
                filterChainManager.addFilter( entry.getKey(), entry.getValue(), true );
            }
            return filterChainManager;
        }
    }

    /**
     * Simple {@link FilterConfig} that delegates to the surrounding {@link ServletContext}.
     */
    private static final class SimpleFilterConfig
        implements FilterConfig
    {
        private final String filterName;

        private final ServletContext servletContext;

        SimpleFilterConfig( String filterName, ServletContext servletContext )
        {
            this.filterName = filterName;
            this.servletContext = servletContext;
        }

        public String getFilterName()
        {
            return filterName;
        }

        public ServletContext getServletContext()
        {
            return servletContext;
        }

        public String getInitParameter( String name )
        {
            return servletContext.getInitParameter( name );
        }

        public Enumeration<?> getInitParameterNames()
        {
            return servletContext.getInitParameterNames();
        }
    }
}
