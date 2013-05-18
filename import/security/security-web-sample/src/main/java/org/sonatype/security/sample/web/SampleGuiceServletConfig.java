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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.shiro.guice.web.ShiroWebModule;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.sample.web.services.SampleService;
import org.sonatype.security.web.guice.SecurityWebFilter;
import org.sonatype.security.web.guice.SecurityWebModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.SitebricksServletModule;

public class SampleGuiceServletConfig
    extends GuiceServletContextListener
{
    private Injector injector = null;

    private ShiroWebModule shiroWebModule;

    @Override
    protected Injector getInjector()
    {
        if ( injector == null )
        {
            injector = Guice.createInjector( getWireModule() );
        }

        return injector;
    }

    @Override
    public void contextInitialized( ServletContextEvent servletContextEvent )
    {
        shiroWebModule = getShiroModule( servletContextEvent.getServletContext() );

        // start security and verify that the realm and web security managers are the same instance
        WebSecurityManager webSecurityManager = getInjector().getInstance( WebSecurityManager.class );
        SecuritySystem securitySystem = getInjector().getInstance( SecuritySystem.class );

        securitySystem.start();

        assert ( securitySystem.getSecurityManager() == webSecurityManager ) : "SecuritySystem.securityManager != WebSecurityManager";

        super.contextInitialized( servletContextEvent );
    }

    protected Module getWireModule()
    {
        ClassSpace space = new URLClassSpace( getClass().getClassLoader() );

        // order matters; shiro must be first so its bindings take priority over any bindings discovered during scanning
        return new WireModule( shiroWebModule, new SpaceModule( space ), getPropertiesModule(), getSitebricksModule() );
    }

    protected ShiroWebModule getShiroModule( ServletContext servletContext )
    {
        return new SecurityWebModule( servletContext, false )
        {
            @Override
            @SuppressWarnings( "unchecked" )
            protected void configureShiroWeb()
            {
                super.configureShiroWeb();

                addFilterChain( "/test", AUTHC_BASIC, config( REST, "sample:priv-name" ) );
                addFilterChain( "/**", AUTHC_BASIC, config( REST, "sample:permToCatchAllUnprotecteds" ) );
            }
        };
    }

    protected AbstractModule getSitebricksModule()
    {
        return new SitebricksModule()
        {
            @Override
            protected void configureSitebricks()
            {
                scan( SampleService.class.getPackage() );
            }

            @Override
            protected SitebricksServletModule servletModule()
            {
                return new SitebricksServletModule()
                {
                    @Override
                    protected void configurePreFilters()
                    {
                        filter( "/*" ).through( SecurityWebFilter.class );
                    }
                };
            }
        };
    }

    protected AbstractModule getPropertiesModule()
    {
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                binder().bind( ParameterKeys.PROPERTIES ).toInstance( getProperties() );
            }
        };
    }

    protected Properties getProperties()
    {
        Properties properties = new Properties();
        try
        {
            properties.load( getClass().getClassLoader().getResourceAsStream( "config.properties" ) );
        }
        catch ( IOException e )
        {
            // ignore...
        }
        return properties;
    }
}
