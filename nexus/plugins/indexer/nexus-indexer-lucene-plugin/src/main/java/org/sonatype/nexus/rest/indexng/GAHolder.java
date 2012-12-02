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

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.maven.index.artifact.VersionUtils;
import org.sonatype.aether.version.Version;
import org.sonatype.nexus.rest.model.NexusNGArtifact;

class GAHolder
{
    private final SortedMap<Version, NexusNGArtifact> versionHits = new TreeMap<Version, NexusNGArtifact>();

    private NexusNGArtifact latestSnapshot = null;

    private Version latestSnapshotVersion = null;

    private NexusNGArtifact latestRelease = null;

    private Version latestReleaseVersion = null;

    public NexusNGArtifact getVersionHit( Version version )
    {
        return versionHits.get( version );
    }

    public void putVersionHit( Version version, NexusNGArtifact versionHit )
    {
        versionHits.put( version, versionHit );

        if ( VersionUtils.isSnapshot( versionHit.getVersion() ) )
        {
            if ( latestSnapshotVersion == null || latestReleaseVersion.compareTo( version ) > 0 )
            {
                latestSnapshot = versionHit;
                latestSnapshotVersion = version;
            }
        }
        else
        {
            if ( latestReleaseVersion == null || latestReleaseVersion.compareTo( version ) > 0 )
            {
                latestRelease = versionHit;
                latestReleaseVersion = version;
            }
        }
    }

    public NexusNGArtifact getLatestRelease()
    {
        return latestRelease;
    }

    public NexusNGArtifact getLatestSnapshot()
    {
        return latestSnapshot;
    }

    public Collection<NexusNGArtifact> getOrderedVersionHits()
    {
        return versionHits.values();
    }

    public NexusNGArtifact getLatestVersionHit()
    {
        return latestRelease != null ? latestRelease : latestSnapshot;
    }
}
