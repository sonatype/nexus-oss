/**
 * Copyright © 2007-2008 Sonatype, Inc. All rights reserved.
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

    public boolean addArtifactInfo( Map<String, ArtifactInfoGroup> result, ArtifactInfo artifactInfo )
    {
        String key = getGroupKey( artifactInfo );

        ArtifactInfoGroup group = result.get( key );

        if ( group == null )
        {
            group = new ArtifactInfoGroup( key, comparator );
            
            result.put( key, group );
        }

        return group.addArtifactInfo( artifactInfo );
    }

    protected abstract String getGroupKey( ArtifactInfo artifactInfo );

}
