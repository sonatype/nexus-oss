package org.sonatype.nexus.index;

import java.util.List;

import org.sonatype.nexus.index.context.IndexingContext;

/**
 * This is a aggregated artifact info filter that performs AND operation (all filter has to accept the artifact info, if one
 * rejects, results is reject). It is implemented in "fail fast" way, as soon as some member ArtifactFilter rejects, it
 * will be rejected.
 * 
 * @author cstamas
 */
public class AndMultiArtifactInfoFilter
    extends AbstractMultiArtifactInfoFilter
{
    public AndMultiArtifactInfoFilter( List<ArtifactInfoFilter> filters )
    {
        super( filters );
    }

    @Override
    protected boolean accepts( List<ArtifactInfoFilter> filters, IndexingContext ctx, ArtifactInfo ai )
    {
        for ( ArtifactInfoFilter filter : filters )
        {
            if ( !filter.accepts( ctx, ai ) )
            {
                return false;
            }

        }

        return true;
    }
}
