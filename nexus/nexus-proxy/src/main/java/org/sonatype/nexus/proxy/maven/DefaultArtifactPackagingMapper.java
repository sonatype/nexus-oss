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
package org.sonatype.nexus.proxy.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * A very simple artifact packaging mapper, that has everyting for quick-start wired in this class. Also, it takes into
 * account the "${nexus-work}/conf/packaging2extension-mapping.properties" file into account if found. To override the
 * "defaults" in this class, simply add lines to properties file with same keys.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultArtifactPackagingMapper
    extends AbstractLogEnabled
    implements ArtifactPackagingMapper
{
    public static final String MAPPING_PROPERTIES_FILE = "packaging2extension-mapping.properties";

    /**
     * @plexus.requirement
     */
    private ApplicationConfiguration applicationConfiguration;

    private Map<String, String> packaging2extensionMapping;

    private final static Map<String, String> defaults;

    static
    {
        defaults = new HashMap<String, String>();
        defaults.put( "ejb-client", "jar" );
        defaults.put( "ejb", "jar" );
        defaults.put( "rar", "jar" );
        defaults.put( "par", "jar" );
        defaults.put( "maven-plugin", "jar" );
        defaults.put( "maven-archetype", "jar" );
        defaults.put( "plexus-application", "jar" );
    }

    public Map<String, String> getPackaging2extensionMapping()
    {
        if ( packaging2extensionMapping == null )
        {
            packaging2extensionMapping = new HashMap<String, String>();

            // merge defaults
            packaging2extensionMapping.putAll( defaults );

            // if user file exists, add it too
            File propertiesFile = new File(
                applicationConfiguration.getConfigurationDirectory(),
                MAPPING_PROPERTIES_FILE );

            if ( propertiesFile.exists() )
            {
                getLogger().info( "Found user mappings file, applying it..." );

                Properties userMappings = new Properties();

                FileInputStream fis = null;

                try
                {
                    fis = new FileInputStream( propertiesFile );

                    userMappings.load( fis );

                    if ( userMappings.keySet().size() > 0 )
                    {
                        for ( Object key : userMappings.keySet() )
                        {
                            packaging2extensionMapping.put( key.toString(), userMappings.getProperty( key.toString() ) );
                        }

                        getLogger().info(
                            propertiesFile.getAbsolutePath() + " user mapping file contained "
                                + userMappings.keySet().size() + " mappings, applied them all succesfully." );
                    }
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Got IO exception during read of file: " + propertiesFile.getAbsolutePath() );
                }
                finally
                {
                    IOUtil.close( fis );
                }

            }
            else
            {
                getLogger().info( "User mappings file not found, will work with defaults..." );
            }
        }

        return packaging2extensionMapping;
    }

    public void setPackaging2extensionMapping( Map<String, String> packaging2extensionMapping )
    {
        this.packaging2extensionMapping = packaging2extensionMapping;
    }

    public Map<String, String> getDefaults()
    {
        return defaults;
    }

    public String getExtensionForPackaging( String packaging )
    {
        if ( getPackaging2extensionMapping().containsKey( packaging ) )
        {
            return getPackaging2extensionMapping().get( packaging );
        }
        else
        {
            // default's to packaging name, ie. "jar", "war", "pom", etc.
            return packaging;
        }
    }
}
