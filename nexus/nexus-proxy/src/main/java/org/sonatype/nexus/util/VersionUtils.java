package org.sonatype.nexus.util;

import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;

public class VersionUtils
{
    private static final Pattern VERSION_FILE_PATTERN = Pattern.compile( "^(.*)-([0-9]{8}.[0-9]{6})-([0-9]+)$" );
    
    public static boolean isSnapshot( String baseVersion )
    {
        synchronized ( VERSION_FILE_PATTERN )
        {
            return VERSION_FILE_PATTERN.matcher( baseVersion ).matches()
            || baseVersion.endsWith( Artifact.SNAPSHOT_VERSION );   
        }
    }
}
