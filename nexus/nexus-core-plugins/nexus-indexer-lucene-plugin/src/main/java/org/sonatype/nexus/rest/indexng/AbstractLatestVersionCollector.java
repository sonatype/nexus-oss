package org.sonatype.nexus.rest.indexng;

import java.util.HashMap;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoFilter;
import org.sonatype.nexus.index.context.IndexingContext;

public abstract class AbstractLatestVersionCollector
    implements ArtifactInfoFilter
{
    private HashMap<String, LatestVersionHolder> lvhs = new HashMap<String, LatestVersionHolder>();

    public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
    {
        final String key = getKeyFromAi( ctx, ai );

        LatestVersionHolder lvh = lvhs.get( key );

        if ( lvh == null )
        {
            lvh = createLVH( ctx, ai );

            lvhs.put( key, lvh );
        }

        lvh.maintainLatestVersions( ai );

        return true;
    }

    public LatestVersionHolder getLVHForKey( String key )
    {
        return lvhs.get( key );
    }

    public abstract LatestVersionHolder createLVH( IndexingContext ctx, ArtifactInfo ai );

    public abstract String getKeyFromAi( IndexingContext ctx, ArtifactInfo ai );

    // ==

    protected HashMap<String, LatestVersionHolder> getLvhs()
    {
        return lvhs;
    }
}
