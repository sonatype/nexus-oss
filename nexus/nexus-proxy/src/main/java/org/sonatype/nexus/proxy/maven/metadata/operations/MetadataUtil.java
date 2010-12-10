package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.util.List;

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

}
