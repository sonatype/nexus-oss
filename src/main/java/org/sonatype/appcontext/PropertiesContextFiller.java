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
public class PropertiesContextFiller
    implements ContextFiller
{
    private File plexusPropertiesFile;

    private boolean failIfNotFound = false;

    public PropertiesContextFiller()
    {
        this( null );
    }

    public PropertiesContextFiller( File plexusPropertiesFile )
    {
        this( plexusPropertiesFile, true );
    }

    public PropertiesContextFiller( File plexusPropertiesFile, boolean failIfNotFound )
    {
        this.plexusPropertiesFile = plexusPropertiesFile;

        this.failIfNotFound = failIfNotFound;
    }

    public void fillContext( AppContextFactory factory, AppContextRequest request, Map<Object, Object> context )
        throws AppContextException
    {
        try
        {
            if ( plexusPropertiesFile.exists() )
            {
                Properties containerProperties = new Properties();

                containerProperties.load( new FileInputStream( plexusPropertiesFile ) );

                context.putAll( containerProperties );
            }
            else if ( isFailIfNotFound() )
            {
                throw new AppContextException( "Cannot load up plexus properties file from \""
                    + plexusPropertiesFile.getAbsolutePath() + "\", it does not exists!" );
            }
        }
        catch ( IOException e )
        {
            throw new AppContextException( "Cannot load up plexus properties file from \""
                + plexusPropertiesFile.getAbsolutePath() + "\"!", e );
        }
    }

    public File getPlexusPropertiesFile()
    {
        return plexusPropertiesFile;
    }

    public void setPlexusPropertiesFile( File plexusPropertiesFile )
    {
        this.plexusPropertiesFile = plexusPropertiesFile;
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
