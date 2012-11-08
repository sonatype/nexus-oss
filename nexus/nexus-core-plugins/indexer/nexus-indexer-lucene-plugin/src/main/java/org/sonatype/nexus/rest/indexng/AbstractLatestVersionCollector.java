/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.indexng;

import java.util.HashMap;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.context.IndexingContext;

public abstract class AbstractLatestVersionCollector
    implements ArtifactInfoFilter
{
    private HashMap<String, LatestVersionHolder> lvhs = new HashMap<String, LatestVersionHolder>();

    public boolean accepts( IndexingContext ctx, ArtifactInfo ai )
    {
        final String key = getKeyFromAi( ctx, ai );

        LatestVersionHolder lvh = lvhs.get( key );

        if ( lvh == null )
        {
            lvh = createLVH( ctx, ai );

            lvhs.put( key, lvh );
        }

        lvh.maintainLatestVersions( ai );

        return true;
    }

    public LatestVersionHolder getLVHForKey( String key )
    {
        return lvhs.get( key );
    }

    public abstract LatestVersionHolder createLVH( IndexingContext ctx, ArtifactInfo ai );

    public abstract String getKeyFromAi( IndexingContext ctx, ArtifactInfo ai );

    // ==

    protected HashMap<String, LatestVersionHolder> getLvhs()
    {
        return lvhs;
    }
}
