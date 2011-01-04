/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.maven.metadata.operations;

import java.util.List;

import org.apache.maven.artifact.repository.metadata.Plugin;
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

    public static boolean isPluginEquals( Plugin p1, Plugin p2 )
    {
        if ( p1.getName() == null )
        {
            p1.setName( "" );
        }

        if ( p2.getName() == null )
        {
            p2.setName( "" );
        }

        if ( StringUtils.equals( p1.getArtifactId(), p2.getArtifactId() )
            && StringUtils.equals( p1.getPrefix(), p2.getPrefix() ) && StringUtils.equals( p1.getName(), p2.getName() ) )
        {
            return true;
        }

        return false;
    }

}
