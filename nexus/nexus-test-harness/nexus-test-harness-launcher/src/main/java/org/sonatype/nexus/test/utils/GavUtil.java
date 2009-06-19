package org.sonatype.nexus.test.utils;

import java.io.FileNotFoundException;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;

public class GavUtil
{

    public static Gav newGav( String groupId, String artifactId, String version )
    {
        return newGav( groupId, artifactId, version, "jar" );
    }

    public static Gav newGav( String groupId, String artifactId, String version, String packging )
    {
        return new Gav( groupId, artifactId, version, null, packging, null, null, null, false, false, null, false, null );
    }

    public static String getRelitivePomPath( Gav gav )
        throws FileNotFoundException
    {
        return getRelitiveArtifactPath( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), "pom", null );
    }

    public static String getRelitiveArtifactPath( Gav gav )
        throws FileNotFoundException
    {
        return getRelitiveArtifactPath( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(), gav.getExtension(),
                                        gav.getClassifier() );
    }

    public static String getRelitiveArtifactPath( String groupId, String artifactId, String version, String extension,
                                                  String classifier )
        throws FileNotFoundException
    {
        String classifierPart = StringUtils.isEmpty( classifier ) ? "" : "-" + classifier;
        return groupId.replace( '.', '/' ) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version
            + classifierPart + "." + extension;
    }

}
