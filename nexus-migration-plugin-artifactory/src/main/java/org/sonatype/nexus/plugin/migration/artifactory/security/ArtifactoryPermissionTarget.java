/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.List;

public class ArtifactoryPermissionTarget
{
    private static int defaultIdCount = 1001;

    private String id;

    private String repoKey;

    private List<String> includes = new ArrayList<String>();

    private List<String> excludes = new ArrayList<String>();

    public ArtifactoryPermissionTarget( String repoKey )
    {
        this.id = "arti-perm-target-" + defaultIdCount;

        defaultIdCount++;

        this.repoKey = repoKey;
    }
    
    public ArtifactoryPermissionTarget( String id, String repoKey )
    {
        this.id = id;

        this.repoKey = repoKey;
    }

    public String getId()
    {
        return id;
    }

    public String getRepoKey()
    {
        return repoKey;
    }

    public List<String> getIncludes()
    {
        return includes;
    }

    public List<String> getExcludes()
    {
        return excludes;
    }

    public void addInclude( String include )
    {
        includes.add( include );
    }

    public void addExclude( String exclude )
    {
        excludes.add( exclude );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof ArtifactoryPermissionTarget ) )
        {
            return false;
        }

        ArtifactoryPermissionTarget repoTarget = (ArtifactoryPermissionTarget) obj;

        return id.equals( repoTarget.id ) && repoKey.equals( repoTarget.repoKey )
            && includes.equals( repoTarget.includes ) && excludes.equals( repoTarget.excludes );
    }

}
