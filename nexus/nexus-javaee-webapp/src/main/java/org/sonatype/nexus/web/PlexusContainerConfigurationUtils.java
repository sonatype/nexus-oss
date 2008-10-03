/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */

package org.sonatype.nexus.web;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
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

    private Map<String, String> buildContext( ServletContext servletContext )
    {
        servletContext.log( "Loading plexus context properties from: '" + DEFAULT_PLEXUS_PROPERTIES + "'" );

        Map<String, String> context = new HashMap<String, String>();

        String baseDir = servletContext.getRealPath( "/WEB-INF" );

        if ( !StringUtils.isEmpty( baseDir ) )
        {
            servletContext.log( "Setting Plexus basedir to: " + baseDir );

            context.put( PLEXUS_HOME, baseDir );
        }
        else
        {
            servletContext.log( "CANNOT set Plexus basedir! (are we in unpacked WAR?)" );
        }

        String plexusPropertiesPath = servletContext.getInitParameter( PLEXUS_PROPERTIES_PARAM );

        if ( plexusPropertiesPath == null )
        {
            plexusPropertiesPath = DEFAULT_PLEXUS_PROPERTIES;
        }

        try
        {
            URL url = servletContext.getResource( plexusPropertiesPath );

            Properties properties = PropertyUtils.loadProperties( url );

            if ( properties == null )
            {
                throw new Exception( "Could not locate url: " + url.toString() );
            }

            for ( Object key : properties.keySet() )
            {
                context.put( key.toString(), properties.getProperty( key.toString() ) );
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
