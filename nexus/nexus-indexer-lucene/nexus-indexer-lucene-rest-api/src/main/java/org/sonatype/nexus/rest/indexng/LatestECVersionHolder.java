package org.sonatype.nexus.rest.indexng;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.index.ArtifactInfo;

/**
 * Extended version of latest version holder, that collects Extension+Classifier combinations too.
 * 
 * @author cstamas
 */
public class LatestECVersionHolder
    extends LatestVersionHolder
{
    private final Set<ECHolder> ecHolders;

    public LatestECVersionHolder( ArtifactInfo ai )
    {
        super( ai );

        this.ecHolders = new HashSet<ECHolder>();
    }

    public boolean maintainLatestVersions( final ArtifactInfo ai )
    {
        boolean versionChanged = super.maintainLatestVersions( ai );

        if ( versionChanged )
        {
            ecHolders.clear();
        }

        ecHolders.add( new ECHolder( ai.fextension, ai.classifier ) );

        return versionChanged;
    }

    public Set<ECHolder> getEcHolders()
    {
        return ecHolders;
    }
}
