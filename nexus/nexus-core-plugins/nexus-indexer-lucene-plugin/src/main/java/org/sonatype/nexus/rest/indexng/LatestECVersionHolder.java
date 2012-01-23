/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.indexng;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.artifact.VersionUtils;

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
