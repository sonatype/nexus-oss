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
package org.sonatype.security.web.guice;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authz.HttpMethodPermissionFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanScanning;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.web.ProtectedPathManager;
import org.sonatype.sisu.ehcache.CacheManagerComponent;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Verifies functionality of SecurityWebModule.
 * 
 * @since 2.7
 */
public class SecurityWebModuleTest
{
    private Injector injector;

    @Before
    public void setUp()
    {
        injector = Guice.createInjector( getWireModule() );
    }

    @Test
    public void testInjectionIsSetupCorrectly()
    {
        SecuritySystem securitySystem = injector.getInstance( SecuritySystem.class );

        SecurityManager securityManager = injector.getInstance( SecurityManager.class );

        RealmSecurityManager realmSecurityManager =
            (RealmSecurityManager) injector.getInstance( WebSecurityManager.class );

        assertThat( securitySystem.getSecurityManager(), sameInstance( securityManager ) );
        assertThat( securitySystem.getSecurityManager(), sameInstance( realmSecurityManager ) );

        assertThat( securityManager, instanceOf( DefaultWebSecurityManager.class ) );
        DefaultSecurityManager defaultSecurityManager = (DefaultSecurityManager) securityManager;

        assertThat( defaultSecurityManager.getSessionManager(), instanceOf( DefaultWebSessionManager.class ) );
        DefaultSessionManager sessionManager = (DefaultSessionManager) defaultSecurityManager.getSessionManager();
        assertThat( sessionManager.getSessionDAO(), instanceOf( EnterpriseCacheSessionDAO.class ) );

        SecurityWebFilter shiroFilter = injector.getInstance( SecurityWebFilter.class );
        assertThat( shiroFilter.getFilterChainResolver(), instanceOf( PathMatchingFilterChainResolver.class ) );

        PathMatchingFilterChainResolver filterChainResolver =
            (PathMatchingFilterChainResolver) shiroFilter.getFilterChainResolver();
        assertThat( filterChainResolver.getFilterChainManager(), instanceOf( DefaultFilterChainManager.class ) );
        assertThat( filterChainResolver, sameInstance( injector.getInstance( FilterChainResolver.class ) ) );

        // now add a protected path
        ProtectedPathManager protectedPathManager = injector.getInstance( ProtectedPathManager.class );
        protectedPathManager.addProtectedResource( "/service/**", "foobar,perms[sample:priv-name]" );

        NamedFilterList filterList = filterChainResolver.getFilterChainManager().getChain( "/service/**" );
        assertThat( filterList.get( 0 ), instanceOf( SimpleAccessControlFilter.class ) );
        assertThat( filterList.get( 1 ), instanceOf( HttpMethodPermissionFilter.class ) );

        // test that injection of filters works
        assertThat( ( (SimpleAccessControlFilter) filterList.get( 0 ) ).getSecurityXMLFilePath(),
                    equalTo( "target/foo/security.xml" ) );
    }

    @After
    public void stopCache()
    {
        if ( injector != null )
        {
            injector.getInstance( CacheManagerComponent.class ).shutdown();
        }
    }

    private Module getWireModule()
    {
        return new WireModule( getShiroModule(), getSpaceModule(), getPropertiesModule() );
    }

    private Module getShiroModule()
    {
        return new SecurityWebModule( createMock( ServletContext.class ), true )
        {
            @Override
            protected void configureShiroWeb()
            {
                super.configureShiroWeb();

                SimpleAccessControlFilter foobar = new SimpleAccessControlFilter();
                foobar.setApplicationName( "Foobar Application" );

                bindNamedFilter( "foobar", foobar );
                bindNamedFilter( "perms", new HttpMethodPermissionFilter() );
            }
        };
    }

    private Module getSpaceModule()
    {
        return new SpaceModule( new URLClassSpace( getClass().getClassLoader() ), BeanScanning.INDEX );
    }

    protected AbstractModule getPropertiesModule()
    {
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put( "security-xml-file", "target/foo/security.xml" );
                properties.put( "application-conf", "target/plexus-home/conf" );
                binder().bind( ParameterKeys.PROPERTIES ).toInstance( properties );
            }
        };
    }

    static class SimpleAccessControlFilter
        extends BasicHttpAuthenticationFilter
    {
        @Inject
        @Named( "${security-xml-file}" )
        private String securityXMLFilePath;

        public String getSecurityXMLFilePath()
        {
            return securityXMLFilePath;
        }
    }
}
