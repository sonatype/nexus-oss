package org.sonatype.appcontext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * A context filler that uses Java properties file as context source. This filler should be set-up before using! If the
 * File set is absolute, it will used as is. If the file set is relative, the basedir will be used to resolve it.
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
        File file = null;

        if ( getPropertiesFile().isAbsolute() )
        {
            file = getPropertiesFile();
        }
        else
        {
            file = new File( request.getBasedirDiscoverer().discoverBasedir(), getPropertiesFile().getPath() );
        }

        try
        {
            if ( file.exists() )
            {
                Properties containerProperties = new Properties();

                containerProperties.load( new FileInputStream( file ) );

                context.putAll( containerProperties );
            }
            else if ( isFailIfNotFound() )
            {
                throw new AppContextException( "Cannot load up plexus properties file from \"" + file.getAbsolutePath()
                    + "\", it does not exists!" );
            }
        }
        catch ( IOException e )
        {
            throw new AppContextException( "Cannot load up plexus properties file from \"" + file.getAbsolutePath()
                + "\"!", e );
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
