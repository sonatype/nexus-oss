package org.sonatype.nexus.proxy.maven;
/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * A very simple artifact packaging mapper, that has everyting for quick-start wired in this class. Also, it takes into
 * account the "${nexus-work}/conf/packaging2extension-mapping.properties" file into account if found. To override the
 * "defaults" in this class, simply add lines to properties file with same keys.
 * 
 * @author cstamas
 */
@Component(role=ArtifactPackagingMapper.class)
public class DefaultArtifactPackagingMapper
    extends AbstractLogEnabled
    implements ArtifactPackagingMapper
{
    public static final String MAPPING_PROPERTIES_FILE = "packaging2extension-mapping.properties";

    @Requirement
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
        defaults.put( "eclipse-plugin", "jar" );
        defaults.put( "eclipse-feature", "jar" );
        defaults.put( "eclipse-application", "zip" );
        defaults.put( "nexus-plugin", "jar" );
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
