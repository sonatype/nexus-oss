package org.sonatype.appcontext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * A context filler that uses Java properties file as context source. This filler should be set-up before using!
 * 
 * @author cstamas
 */
public class PropertiesFileContextFiller
    implements ContextFiller
{
    private File propertiesFile;

    private boolean failIfNotFound = false;

    public PropertiesFileContextFiller()
    {
        this( null );
    }

    public PropertiesFileContextFiller( File propertiesFile )
    {
        this( propertiesFile, true );
    }

    public PropertiesFileContextFiller( File propertiesFile, boolean failIfNotFound )
    {
        this.propertiesFile = propertiesFile;

        this.failIfNotFound = failIfNotFound;
    }

    public void fillContext( AppContextFactory factory, AppContextRequest request, Map<Object, Object> context )
        throws AppContextException
    {
        try
        {
            if ( propertiesFile.exists() )
            {
                Properties containerProperties = new Properties();

                containerProperties.load( new FileInputStream( propertiesFile ) );

                context.putAll( containerProperties );
            }
            else if ( isFailIfNotFound() )
            {
                throw new AppContextException( "Cannot load up plexus properties file from \""
                    + propertiesFile.getAbsolutePath() + "\", it does not exists!" );
            }
        }
        catch ( IOException e )
        {
            throw new AppContextException( "Cannot load up plexus properties file from \""
                + propertiesFile.getAbsolutePath() + "\"!", e );
        }
    }

    public File getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile( File plexusPropertiesFile )
    {
        this.propertiesFile = plexusPropertiesFile;
    }

    public boolean isFailIfNotFound()
    {
        return failIfNotFound;
    }

    public void setFailIfNotFound( boolean failIfNotFound )
    {
        this.failIfNotFound = failIfNotFound;
    }
}
