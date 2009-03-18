package org.sonatype.nexus.test.utils;

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

}
