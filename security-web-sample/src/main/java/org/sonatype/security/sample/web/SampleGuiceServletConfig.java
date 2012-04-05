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
package org.sonatype.security.sample.web;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.sonatype.guice.bean.binders.ParameterKeys;
import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.binders.WireModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.security.sample.web.services.SampleService;
import org.sonatype.security.web.ShiroSecurityFilter;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;

public class SampleGuiceServletConfig
    extends GuiceServletContextListener
{
    private Injector injector = null;

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
        servletContextEvent.getServletContext().setAttribute( ShiroSecurityFilter.INJECTORY_KEY, getInjector() );
        super.contextInitialized( servletContextEvent );
    }

    protected Module getWireModule()
    {
        ClassSpace space = new URLClassSpace( (URLClassLoader) getClass().getClassLoader() );

        return new WireModule( new SpaceModule( space ), getPropertiesModule(), getSitebricksModule() );
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
        };
    }

    protected AbstractModule getPropertiesModule()
    {
        return new AbstractModule()
        {
            @SuppressWarnings( { "rawtypes", "unchecked" } )
            @Override
            protected void configure()
            {
                Properties properties = getProperties();

                binder().bind( ParameterKeys.PROPERTIES ).toInstance( (Map) properties );
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
        }

        return properties;
    }
}
