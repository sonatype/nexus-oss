/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;

public interface MetadataManager
{
    /**
     * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void deployArtifact( ArtifactStoreRequest request )
        throws IOException,
            IllegalArtifactCoordinateException;

    /**
     * Calling this method updates the GAV, GA and G metadatas accordingly. It senses whether it is a snapshot or not.
     * 
     * @param req
     */
    void undeployArtifact( ArtifactStoreRequest request )
        throws IOException,
            IllegalArtifactCoordinateException;

    /**
     * Resolves the artifact, honoring LATEST and RELEASE as version. In case of snapshots, it will try to resolve the
     * timestamped version too, if needed.
     * 
     * @return
     * @throws IOException
     * @throws IllegalArtifactCoordinateException
     */
    Gav resolveArtifact( ArtifactStoreRequest gavRequest )
        throws IOException,
            IllegalArtifactCoordinateException;

    /**
     * Resolves the snapshot base version to a timestamped version if possible. Only when a repo is snapshot.
     * 
     * @return
     * @throws IOException
     * @throws IllegalArtifactCoordinateException
     */
    Gav resolveSnapshot( ArtifactStoreRequest gavRequest, Gav gav )
        throws IOException,
            IllegalArtifactCoordinateException;
}
