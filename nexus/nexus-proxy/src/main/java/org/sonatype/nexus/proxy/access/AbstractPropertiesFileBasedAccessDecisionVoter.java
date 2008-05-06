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
            result.load( new FileInputStream( resourceFile ) );

            return result;
        }

        // First see if the resource is a valid file
        resourceFile = new File( resource );

        if ( resourceFile.exists() )
        {
            result.load( new FileInputStream( resourceFile ) );

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
