package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtifactoryFileFilder
    implements FileFilter
{

    private boolean allowSnapshot;

    public ArtifactoryFileFilder( boolean allowSnapshot )
    {
        this.allowSnapshot = allowSnapshot;
    }

    String LATEST_VERSION = "LATEST";

    String SNAPSHOT_VERSION = "SNAPSHOT";

    Pattern VERSION_FILE_PATTERN = Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );

    public boolean isSnapshot( String version )
    {
        Matcher m = VERSION_FILE_PATTERN.matcher( version );
        if ( m.matches() )
        {
            return true;
        }
        else
        {
            return version.endsWith( SNAPSHOT_VERSION ) || version.equals( LATEST_VERSION );
        }
    }

    public boolean accept( File pathname )
    {
        if ( pathname.getName().endsWith( ".artifactory-metadata" ) )
        {
            return false;
        }

        String version = pathname.getParentFile().getName();
        return allowSnapshot && isSnapshot( version );
    }

}
