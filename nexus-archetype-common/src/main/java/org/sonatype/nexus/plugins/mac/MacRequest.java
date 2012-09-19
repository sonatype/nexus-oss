/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
 * 
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.mac;

import org.apache.maven.index.ArtifactInfoFilter;

/**
 * A request carrying needed information to construct response Archetype Catalog properly.
 * 
 * @author cstamas
 */
public class MacRequest
{
    private final String repositoryId;

    private final String repositoryUrl;

    private final ArtifactInfoFilter artifactInfoFilter;

    public MacRequest( String repositoryId )
    {
        this( repositoryId, null, null );
    }

    public MacRequest( final String repositoryId, final String repositoryUrl,
                       final ArtifactInfoFilter artifactInfoFilter )
    {
        this.repositoryId = repositoryId;
        this.repositoryUrl = repositoryUrl;
        this.artifactInfoFilter = artifactInfoFilter;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public ArtifactInfoFilter getArtifactInfoFilter()
    {
        return artifactInfoFilter;
    }
}
