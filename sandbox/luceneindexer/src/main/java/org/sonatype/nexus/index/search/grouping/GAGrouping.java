/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.search.grouping;

import java.util.Comparator;

import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.Grouping;

/**
 * A GroupId : ArtifactId {@link Grouping} implementation.
 * 
 * @author Tamas Cservenak
 */
public class GAGrouping
    extends AbstractGrouping
{

    public GAGrouping()
    {
        super();
    }

    public GAGrouping( Comparator<ArtifactInfo> comparator )
    {
        super( comparator );
    }

    @Override
    protected String getGroupKey( ArtifactInfo artifactInfo )
    {
        return artifactInfo.groupId + " : " + artifactInfo.artifactId;
    }

}
