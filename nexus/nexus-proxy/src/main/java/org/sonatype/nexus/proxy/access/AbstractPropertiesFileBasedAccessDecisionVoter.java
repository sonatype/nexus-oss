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
package org.sonatype.nexus.proxy.access;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractPropertiesFileBasedAccessDecisionVoter
    extends AbstractAccessDecisionVoter
{
    public static final String PARAM_PROPERTIES_FILE_PATH = "propertiesFilePath";

    /** The properties base. */
    private Properties properties;

    /**
     * Gets the properties path.
     * 
     * @return the properties path
     */
    public String getPropertiesPath()
    {
        return getConfigurationValue( PARAM_PROPERTIES_FILE_PATH );
    }

    /**
     * Gets the properties.
     * 
     * @return the properties
     */
    public Properties getProperties()
    {
        if ( properties == null )
        {
            try
            {
                properties = loadProperties( getPropertiesPath() );
            }
            catch ( IOException e )
            {
                throw new IllegalArgumentException( "Could not initialize voter!", e );
            }
        }

        return properties;
    }

    /**
     * Load properties.
     * 
     * @param resource the resource
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected Properties loadProperties( String resource )
        throws IOException
    {
        if ( resource == null )
        {
            throw new IllegalArgumentException( "Authorization source properties file path cannot be 'null'!" );
        }

        Properties result = new Properties();
        
        File resourceFile = null;

        // try to get it acainst config dir
        resourceFile = new File( getConfigurationDir(), resource );

        if ( resourceFile.exists() )
        {
            FileInputStream fis = new FileInputStream( resourceFile );
            try
            {
                result.load( fis );
            }
            finally
            {
                fis.close();
            }

            return result;
        }

        // First see if the resource is a valid file
        resourceFile = new File( resource );

        if ( resourceFile.exists() )
        {
            FileInputStream fis = new FileInputStream( resourceFile );
            try
            {
                result.load( fis );
            }
            finally
            {
                fis.close();
            }

            return result;
        }

        // Otherwise try to load it from the classpath
        InputStream is = getClass().getClassLoader().getResourceAsStream( resource );

        if ( is != null )
        {
            result.load( is );

            return result;
        }

        throw new IllegalArgumentException( "Authorization source cannot be loaded because it is not found on "
            + resource );
    }

}
