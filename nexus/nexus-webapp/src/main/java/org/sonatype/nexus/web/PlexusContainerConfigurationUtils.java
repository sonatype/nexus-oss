/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.web;

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
