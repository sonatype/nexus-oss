/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.util;

import java.util.Comparator;

import org.sonatype.nexus.index.ArtifactInfo;

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
