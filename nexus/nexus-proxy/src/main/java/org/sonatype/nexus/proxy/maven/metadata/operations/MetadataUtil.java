package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.util.List;

import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.codehaus.plexus.util.StringUtils;

public class MetadataUtil
{

    public static SnapshotVersion searchForEquivalent( SnapshotVersion source, List<SnapshotVersion> list )
    {
        for ( SnapshotVersion equivalent : list )
        {
            if ( StringUtils.equals( source.getExtension(), equivalent.getExtension() )
                && ( ( StringUtils.isEmpty( source.getClassifier() ) && StringUtils.isEmpty( equivalent.getClassifier() ) ) || StringUtils.equals(
                    source.getClassifier(), equivalent.getClassifier() ) ) )
            {
                return equivalent;
            }
        }
        return null;
    }

    public static boolean isPluginEquals( Plugin p1, Plugin p2 )
    {
        if ( p1.getName() == null )
        {
            p1.setName( "" );
        }

        if ( p2.getName() == null )
        {
            p2.setName( "" );
        }

        if ( StringUtils.equals( p1.getArtifactId(), p2.getArtifactId() )
            && StringUtils.equals( p1.getPrefix(), p2.getPrefix() ) && StringUtils.equals( p1.getName(), p2.getName() ) )
        {
            return true;
        }

        return false;
    }

}
