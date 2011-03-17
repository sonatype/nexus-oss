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
