/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.index;

import org.sonatype.nexus.proxy.maven.gav.Gav;

/**
 * Simple "bridge" utility class that converts Nexus Gav classes into Maven Indexer Gav classes.
 * 
 * @author cstamas
 */
public class GavUtils
{
    public static org.apache.maven.index.artifact.Gav convert( final Gav gav )
    {
        final org.apache.maven.index.artifact.Gav.HashType ht =
            gav.getHashType() != null ? org.apache.maven.index.artifact.Gav.HashType.valueOf( gav.getHashType().name() )
                : null;
        final org.apache.maven.index.artifact.Gav.SignatureType st =
            gav.getSignatureType() != null ? org.apache.maven.index.artifact.Gav.SignatureType.valueOf( gav.getSignatureType().name() )
                : null;

        return new org.apache.maven.index.artifact.Gav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion(),
            gav.getClassifier(), gav.getExtension(), gav.getSnapshotBuildNumber(), gav.getSnapshotTimeStamp(),
            gav.getName(), gav.isHash(), ht, gav.isSignature(), st );
    }

}
