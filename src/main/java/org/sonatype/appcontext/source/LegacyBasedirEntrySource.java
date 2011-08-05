package org.sonatype.appcontext.source;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

public class LegacyBasedirEntrySource
    implements EntrySource, EntrySourceMarker
{
    private final String basedirKey;

    private final boolean failIfNotFound;

    public LegacyBasedirEntrySource()
    {
        this( "basedir", true );
    }

    public LegacyBasedirEntrySource( final String basedirKey, final boolean failIfNotFound )
    {
        this.basedirKey = Preconditions.checkNotNull( basedirKey );

        this.failIfNotFound = failIfNotFound;
    }

    public String getDescription()
    {
        return "legacyBasedir(key:\"" + basedirKey + "\")";
    }

    public EntrySourceMarker getEntrySourceMarker()
    {
        return this;
    }

    public Map<String, Object> getEntries( AppContextRequest request )
        throws AppContextException
    {
        final File baseDir = discoverBasedir( basedirKey );

        if ( failIfNotFound && !baseDir.isDirectory() )
        {
            throw new AppContextException(
                "LegacyBasedirEntrySource was not able to find existing basedir! It discovered \""
                    + baseDir.getAbsolutePath() + "\", but it does not exists or is not a directory!" );
        }

        final HashMap<String, Object> result = new HashMap<String, Object>();

        result.put( basedirKey, baseDir.getAbsolutePath() );

        return result;
    }

    // ==

    public File discoverBasedir( final String basedirKey )
    {
        String basedirPath = System.getProperty( basedirKey );

        if ( basedirPath == null )
        {
            return new File( "" ).getAbsoluteFile();
        }
        else
        {
            return new File( basedirPath ).getAbsoluteFile();
        }
    }
}
