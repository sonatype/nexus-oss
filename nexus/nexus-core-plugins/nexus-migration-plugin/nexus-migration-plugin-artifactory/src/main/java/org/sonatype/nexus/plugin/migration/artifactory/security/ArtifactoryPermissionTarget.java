/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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

    private List<String> repoKeys = new ArrayList<String>();

    private List<String> includes = new ArrayList<String>();

    private List<String> excludes = new ArrayList<String>();

    public ArtifactoryPermissionTarget()
    {
        this.id = "arti-perm-target-" + defaultIdCount;

        defaultIdCount++;
    }

    public ArtifactoryPermissionTarget( String id )
    {
        this.id = id;
    }

    public ArtifactoryPermissionTarget( String id, String repoKey )
    {
        this.id = id;

        this.repoKeys.add( repoKey );
    }

    public List<String> getRepoKeys()
    {
        return repoKeys;
    }

    public void setRepoKeys( List<String> repoKeys )
    {
        this.repoKeys = repoKeys;
    }

    public void addRepoKey( String repoKey )
    {
        if ( !this.repoKeys.contains( repoKey ) )
        {
            repoKeys.add( repoKey );
        }
    }

    public String getId()
    {
        return id;
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

        return id.equals( repoTarget.id ) && repoKeys.equals( repoTarget.repoKeys )
            && includes.equals( repoTarget.includes ) && excludes.equals( repoTarget.excludes );
    }

}
