/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.index;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * An object that holds groupet sets of ArtifactInfos in case of grouped search.
 * 
 * @author cstamas
 */
public class ArtifactInfoGroup
{
    private String groupKey;

    public final Set<ArtifactInfo> artifactInfos;

    public ArtifactInfoGroup( String groupKey )
    {
        this( groupKey, ArtifactInfo.VERSION_COMPARATOR );
    }

    public ArtifactInfoGroup( String groupKey, Comparator<ArtifactInfo> comparator )
    {
        this.groupKey = groupKey;
        this.artifactInfos = new TreeSet<ArtifactInfo>( comparator );
    }

    public String getGroupKey()
    {
        return groupKey;
    }

    public void addArtifactInfo( ArtifactInfo artifactInfo )
    {
        artifactInfos.add( artifactInfo );
    }

    public Set<ArtifactInfo> getArtifactInfos()
    {
        return artifactInfos;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append( groupKey ).append( "=" ).append( artifactInfos.toString() ).toString();
    }

}
