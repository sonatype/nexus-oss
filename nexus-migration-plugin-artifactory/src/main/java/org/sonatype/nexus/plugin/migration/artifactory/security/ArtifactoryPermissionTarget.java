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
