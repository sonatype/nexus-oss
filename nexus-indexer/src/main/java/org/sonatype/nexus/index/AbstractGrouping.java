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
import java.util.Map;

/**
 * The base implementation of Grouping.
 * 
 * @author cstamas
 */
public abstract class AbstractGrouping
    implements Grouping
{
    private Comparator<ArtifactInfo> comparator;

    public AbstractGrouping()
    {
        this( ArtifactInfo.VERSION_COMPARATOR );
    }

    public AbstractGrouping( Comparator<ArtifactInfo> comparator )
    {
        super();
        this.comparator = comparator;
    }

    public void addArtifactInfo( Map<String, ArtifactInfoGroup> result, ArtifactInfo artifactInfo )
    {
        String key = getGroupKey( artifactInfo );

        ArtifactInfoGroup group = result.get( key );

        if ( group == null )
        {
            group = new ArtifactInfoGroup( key, comparator );
            result.put( key, group );
        }

        group.addArtifactInfo( artifactInfo );
    }

    protected abstract String getGroupKey( ArtifactInfo artifactInfo );

}
