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

/**
 * This is the GroupId : ArtifactId : Version grouping.
 * 
 * @author cstamas
 */
public class RGAVGrouping
    extends AbstractGrouping
{

    public RGAVGrouping()
    {
        super();
    }

    public RGAVGrouping( Comparator<ArtifactInfo> comparator )
    {
        super( comparator );
    }

    @Override
    protected String getGroupKey( ArtifactInfo artifactInfo )
    {
        return artifactInfo.repository + ":" + artifactInfo.groupId + ":" + artifactInfo.artifactId + ":"
            + artifactInfo.version + ":" + artifactInfo.classifier;
    }

}
