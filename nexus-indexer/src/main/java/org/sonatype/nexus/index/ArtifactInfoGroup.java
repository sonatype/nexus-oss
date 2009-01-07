/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 * Eugene Kuleshov (Sonatype)
 * Tamas Cservenak (Sonatype)
 * Brian Fox (Sonatype)
 * Jason Van Zyl (Sonatype)
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

    public boolean addArtifactInfo( ArtifactInfo artifactInfo )
    {
        return artifactInfos.add( artifactInfo );
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
