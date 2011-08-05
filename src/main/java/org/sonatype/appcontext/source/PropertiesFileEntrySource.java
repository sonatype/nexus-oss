package org.sonatype.appcontext.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

public class PropertiesFileEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final File propertiesFile;

    private final boolean failIfNotFound;

    public PropertiesFileEntrySource( final File propertiesFile )
    {
        this( propertiesFile, true );
    }

    public PropertiesFileEntrySource( final File propertiesFile, final boolean failIfNotFound )
    {
        this.propertiesFile = Preconditions.checkNotNull( propertiesFile ).getAbsoluteFile();

        this.failIfNotFound = failIfNotFound;
    }

    public String getDescription()
    {
        return "file: " + propertiesFile.getAbsolutePath();
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        try
        {
            FileInputStream fis;

            if ( propertiesFile.isFile() )
            {
                Properties properties = new Properties();

                fis = new FileInputStream( propertiesFile );

                try
                {
                    if ( propertiesFile.getName().endsWith( ".xml" ) )
                    {
                        // assume it's new XML properties file
                        properties.loadFromXML( fis );
                    }
                    else
                    {
                        // assume it's "plain old" properties file
                        properties.load( fis );
                    }
                }
                finally
                {
                    fis.close();
                }

                final Map<String, Object> result = new HashMap<String, Object>();

                for ( Map.Entry<Object, Object> entry : properties.entrySet() )
                {
                    final String key = String.valueOf( entry.getKey() );

                    result.put( key, entry.getValue() );
                }

                return result;
            }
            else if ( failIfNotFound )
            {
                throw new AppContextException( "Cannot load up plexus properties file from \""
                    + propertiesFile.getAbsolutePath() + "\", it does not exists!" );
            }
            else
            {
                return Collections.emptyMap();
            }
        }
        catch ( IOException e )
        {
            throw new AppContextException( "Cannot load up plexus properties file from \""
                + propertiesFile.getAbsolutePath() + "\"!", e );
        }
    }
}
