/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index;

import java.util.Comparator;

/**
 * This is the GroupId : ArtifactId grouping.
 * 
 * @author cstamas
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
