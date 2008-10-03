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

import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.util.PropertyUtils;

public class PlexusContainerConfigurationUtils {

	public static final String DEFAULT_PLEXUS_PROPERTIES = "/WEB-INF/plexus.properties";
	public static final String DEFAULT_PLEXUS_CONFIG = "/WEB-INF/plexus.xml";
	public static final String PLEXUS_HOME = "plexus.home";
	public static final String PLEXUS_PROPERTIES_PARAM = "plexus-properties";
	public static final String PLEXUS_CONFIG_PARAM = "plexus-config";
	
	public ContainerConfiguration buildContainerConfiguration(ServletContext servletContext){
		
		ContainerConfiguration configuration = new DefaultContainerConfiguration();
		
		configuration.setName(null);
		configuration.setContext(buildContext(servletContext));
		configuration.setContainerConfigurationURL(buildConfigurationURL(servletContext));
		
		return configuration;
	}
	
	private Properties buildContext(ServletContext servletContext){
		
		servletContext.log( "Loading plexus context properties from: '" + DEFAULT_PLEXUS_PROPERTIES + "'" );
		String plexusPropertiesPath = servletContext.getInitParameter(PLEXUS_PROPERTIES_PARAM);
		if( plexusPropertiesPath == null){
			plexusPropertiesPath = DEFAULT_PLEXUS_PROPERTIES;
		}
		try
        {
            URL url = servletContext.getResource( plexusPropertiesPath );
            Properties properties = PropertyUtils.loadProperties( url );
            if(properties == null){
            	throw new Exception("Could not locate url: " + url.toString());
            }
            setPlexusHome(servletContext, properties );
            return properties;
        }
        catch ( Exception e ){
        	throw new RuntimeException("Could not load plexus context properties from: '" + plexusPropertiesPath + "'" , e);
        }
	}
	
	private URL buildConfigurationURL(ServletContext servletContext){
       
		servletContext.log( "Loading plexus configuration from: '" + DEFAULT_PLEXUS_CONFIG + "'" );
		String plexusConfigPath = servletContext.getInitParameter(PLEXUS_CONFIG_PARAM);
		if(plexusConfigPath == null){
			plexusConfigPath = DEFAULT_PLEXUS_CONFIG;
		}
		try
        {
            URL url = servletContext.getResource(plexusConfigPath);
            return url;
        }
        catch (Exception e )
        {
        	throw new RuntimeException("Could not load plexus configuration from: '" + plexusConfigPath + "'" , e);
        }
	}
	
    /**
     * Set plexus.home context variable
     */
    private void setPlexusHome(ServletContext context, Properties contextProperties) {
        if ( !contextProperties.containsKey(PLEXUS_HOME )){
			File file = new File(context.getRealPath("/WEB-INF"));
			contextProperties.setProperty(PLEXUS_HOME, file.getAbsolutePath());
        }
	}
}
