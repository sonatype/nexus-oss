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
package org.sonatype.nexus.util;

import java.util.Comparator;

import org.apache.maven.index.ArtifactInfo;

public class ArtifactInfoComparator
    implements Comparator<ArtifactInfo>
{
    private Comparator<String> stringComparator;

    public ArtifactInfoComparator()
    {
        this( String.CASE_INSENSITIVE_ORDER );
    }

    public ArtifactInfoComparator( Comparator<String> nameComparator )
    {
        this.stringComparator = nameComparator;
    }

    public int compare( ArtifactInfo f1, ArtifactInfo f2 )
    {
        int n = stringComparator.compare( f1.groupId, f2.groupId );
        if ( n != 0 )
        {
            return n;
        }

        n = stringComparator.compare( f1.artifactId, f2.artifactId );
        if ( n != 0 )
        {
            return n;
        }

        n = stringComparator.compare( f1.version, f2.version );
        if ( n != 0 )
        {
            return n;
        }

        String c1 = f1.classifier;
        String c2 = f2.classifier;
        if ( c1 == null )
        {
            return c2 == null ? 0 : -1;
        }
        else
        {
            return c2 == null ? 1 : stringComparator.compare( c1, c2 );
        }
    }

}
