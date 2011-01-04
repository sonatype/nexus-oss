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
package org.sonatype.nexus.rest.indexng;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.context.IndexingContext;

/**
 * A special filter that actually does not filter, but collects the latest and release version for every RGA. After
 * iteratorSearchResponse has been processed, this collector will hold all the needed versions of the processed artifact
 * infos.
 * 
 * @author cstamas
 */
public class RepositoryWideLatestVersionCollector
    extends AbstractLatestVersionCollector
{
    @Override
    public LatestVersionHolder createLVH( IndexingContext ctx, ArtifactInfo ai )
    {
        return new LatestECVersionHolder( ai );
    }

    @Override
    public String getKeyFromAi( IndexingContext ctx, ArtifactInfo ai )
    {
        return getKey( ai.repository, ai.groupId, ai.artifactId );
    }

    @Override
    public LatestECVersionHolder getLVHForKey( String key )
    {
        return (LatestECVersionHolder) getLvhs().get( key );
    }

    public String getKey( String repositoryId, String groupId, String artifactId )
    {
        return repositoryId + ":" + groupId + ":" + artifactId;
    }
}
