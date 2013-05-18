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
package org.sonatype.security.sample.web;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.junit.Test;
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.inject.BeanScanning;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.FirstSuccessfulModularRealmAuthenticator;
import org.sonatype.security.authorization.ExceptionCatchingModularRealmAuthorizer;
import org.sonatype.security.realms.XmlRolePermissionResolver;
import org.sonatype.security.web.guice.SecurityWebModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @since 2.7
 */
public class WireUpTest
{
    @Test
    public void testSecurityManager()
    {
        Injector injector = Guice.createInjector( getWireModule() );

        SecurityManager securityManager = injector.getInstance( SecurityManager.class );
        assertThat( securityManager, instanceOf( DefaultWebSecurityManager.class ) );

        RealmSecurityManager realmSecurityManager = injector.getInstance( RealmSecurityManager.class );
        assertThat( realmSecurityManager, instanceOf( DefaultWebSecurityManager.class ) );
        assertThat( realmSecurityManager, sameInstance( securityManager ) );

        DefaultSecurityManager defaultSecurityManager = (DefaultSecurityManager) realmSecurityManager;
        assertThat( defaultSecurityManager.getSessionManager(), instanceOf( DefaultWebSessionManager.class ) );

        DefaultSessionManager sessionManager = (DefaultSessionManager) defaultSecurityManager.getSessionManager();
        assertThat( sessionManager.getSessionDAO(), instanceOf( EnterpriseCacheSessionDAO.class ) );

        ModularRealmAuthenticator authenticator = (ModularRealmAuthenticator) defaultSecurityManager.getAuthenticator();
        assertThat( authenticator, instanceOf( FirstSuccessfulModularRealmAuthenticator.class ) );

        ModularRealmAuthorizer authorizer = (ModularRealmAuthorizer) defaultSecurityManager.getAuthorizer();
        assertThat( authorizer, instanceOf( ExceptionCatchingModularRealmAuthorizer.class ) );
        assertThat( authorizer.getRolePermissionResolver(), instanceOf( XmlRolePermissionResolver.class ) );
    }

    @Test
    public void testSecurityManagerFromSecuritySystem()
    {
        Injector injector = Guice.createInjector( getWireModule() );

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
    }

    private Module getWireModule()
    {
        ClassSpace space = new URLClassSpace( getClass().getClassLoader() );

        return new WireModule( getShiroModule(), new SpaceModule( space, BeanScanning.INDEX ), getPropertiesModule() );
    }

    private static Module getShiroModule()
    {
        return new SecurityWebModule( createMock( ServletContext.class ), false );
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
}
