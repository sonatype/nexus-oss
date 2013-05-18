/*
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
package org.sonatype.plexus.rest;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

public class PlexusContainerConfigurationUtils
{
    public static final String DEFAULT_PLEXUS_PROPERTIES = "/WEB-INF/plexus.properties";

    public static final String DEFAULT_PLEXUS_CONFIG = "/WEB-INF/plexus.xml";

    public static final String PLEXUS_HOME = "basedir";

    public static final String PLEXUS_PROPERTIES_PARAM = "plexus-properties";

    public static final String PLEXUS_CONFIG_PARAM = "plexus-config";

    public ContainerConfiguration buildContainerConfiguration( ServletContext servletContext )
    {
        ContainerConfiguration cc = new DefaultContainerConfiguration()
            .setName( servletContext.getServletContextName() ).setContainerConfigurationURL(
                buildConfigurationURL( servletContext ) ).setContext( buildContext( servletContext ) );

        return cc;
    }

    private Map<Object, Object> buildContext( ServletContext servletContext )
    {
        servletContext.log( "Loading plexus context properties from: '" + DEFAULT_PLEXUS_PROPERTIES + "'" );

        Map<Object, Object> context = new HashMap<Object, Object>();

        String plexusPropertiesPath = servletContext.getInitParameter( PLEXUS_PROPERTIES_PARAM );

        if ( plexusPropertiesPath == null )
        {
            plexusPropertiesPath = DEFAULT_PLEXUS_PROPERTIES;
        }

        try
        {
            URL url = servletContext.getResource( plexusPropertiesPath );

            // if the user has not specified the plexus.props file then just return the context
            if( url == null && plexusPropertiesPath == DEFAULT_PLEXUS_PROPERTIES )
            {
                return context;
            }
            
            Properties properties = PropertyUtils.loadProperties( url );

            if ( properties == null )
            {
                throw new Exception( "Could not locate url: " + url.toString() );
            }

            String baseDir = servletContext.getRealPath( "/WEB-INF" );

            if ( !StringUtils.isEmpty( baseDir ) )
            {
                servletContext.log( "Setting Plexus context variable " + PLEXUS_HOME + " to: " + baseDir );

                properties.put( PLEXUS_HOME, baseDir );
            }
            else
            {
                servletContext.log( "CANNOT set Plexus basedir! (are we in unpacked WAR?)" );
            }

            RegexBasedInterpolator ip = new RegexBasedInterpolator();

            ip.addValueSource( new MapBasedValueSource( properties ) );

            ip.addValueSource( new MapBasedValueSource( System.getProperties() ) );

            for ( Enumeration n = properties.propertyNames(); n.hasMoreElements(); )
            {
                String propertyKey = (String) n.nextElement();

                String propertyValue = ip.interpolate( properties.getProperty( propertyKey ), "" );

                servletContext.log( "Added '" + propertyKey + "=" + propertyValue + "' to Plexus context." );

                context.put( propertyKey, propertyValue );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException(
                "Could not load plexus context properties from: '" + plexusPropertiesPath + "'",
                e );
        }

        return context;
    }

    private URL buildConfigurationURL( ServletContext servletContext )
    {
        servletContext.log( "Loading plexus configuration from: '" + DEFAULT_PLEXUS_CONFIG + "'" );

        String plexusConfigPath = servletContext.getInitParameter( PLEXUS_CONFIG_PARAM );

        if ( plexusConfigPath == null )
        {
            plexusConfigPath = DEFAULT_PLEXUS_CONFIG;
        }

        try
        {
            URL url = servletContext.getResource( plexusConfigPath );

            return url;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Could not load plexus configuration from: '" + plexusConfigPath + "'", e );
        }
    }

}
