package org.sonatype.nexus.index;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.index.context.IndexingContext;

public class UniqueGAArtifactFilterPostprocessor
    implements ArtifactInfoFilter, ArtifactInfoPostprocessor
{
    private static final String VERSION_LATEST = "LATEST";

    private final boolean repositoriesIgnored;

    private final Set<String> gas = new HashSet<String>();

    public UniqueGAArtifactFilterPostprocessor( boolean repositoriesIgnored )
    {
        this.repositoriesIgnored = repositoriesIgnored;
    }

    public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
    {
        String key = ai.groupId + ai.artifactId + ai.classifier;

        if ( !repositoriesIgnored )
        {
            key = ai.repository + key;
        }

        if ( gas.contains( key ) )
        {
            return false;
        }
        else
        {
            gas.add( key );

            postprocess( ctx, ai );

            return true;
        }
    }

    public void postprocess( IndexingContext ctx, ArtifactInfo ai )
    {
        ai.version = VERSION_LATEST;
    }
}
