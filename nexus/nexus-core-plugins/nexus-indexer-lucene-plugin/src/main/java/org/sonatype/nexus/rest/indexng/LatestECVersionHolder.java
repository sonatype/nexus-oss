package org.sonatype.nexus.rest.indexng;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.ArtifactInfo;

/**
 * Extended version of latest version holder, that collects Extension+Classifier combinations too.
 * 
 * @author cstamas
 */
public class LatestECVersionHolder
    extends LatestVersionHolder
{
    private final Set<ECHolder> releaseECHolders;

    private final Set<ECHolder> snapshotECHolders;

    public LatestECVersionHolder( ArtifactInfo ai )
    {
        super( ai );

        this.releaseECHolders = new HashSet<ECHolder>();

        this.snapshotECHolders = new HashSet<ECHolder>();
    }

    public VersionChange maintainLatestVersions( final ArtifactInfo ai )
    {
        VersionChange versionChange = super.maintainLatestVersions( ai );

        if ( VersionUtils.isSnapshot( ai.version ) )
        {
            doMaintainHolder( versionChange, snapshotECHolders, ai );
        }
        else
        {
            doMaintainHolder( versionChange, releaseECHolders, ai );
        }

        return versionChange;
    }

    public Set<ECHolder> getReleaseECHolders()
    {
        return releaseECHolders;
    }

    public Set<ECHolder> getSnapshotECHolders()
    {
        return snapshotECHolders;
    }

    // ==

    protected void doMaintainHolder( final VersionChange versionChange, final Set<ECHolder> holders, final ArtifactInfo ai )
    {
        // we add to collected ECHolders only if version change happened or when version equals to current max
        if ( VersionChange.GREATER.equals( versionChange ) )
        {
            // versiob change happened, clear what we have
            holders.clear();

            // we add it, but after we cleared previous ones, since this one initiated version change
            holders.add( new ECHolder( ai.fextension, ai.classifier ) );
        }
        else if ( VersionChange.EQUALS.equals( versionChange ) )
        {
            // this belongs here, add it
            holders.add( new ECHolder( ai.fextension, ai.classifier ) );
        }
    }

}
