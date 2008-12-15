package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashSet;
import java.util.Set;

public class ArtifactoryAcl
{
    private ArtifactoryRepoPath repoPath;

    private ArtifactoryUser user;

    private Set<ArtifactoryPermission> permissions = new HashSet<ArtifactoryPermission>();

    public ArtifactoryAcl( ArtifactoryRepoPath repoPath, ArtifactoryUser user )
    {
        this.repoPath = repoPath;

        this.user = user;
    }

    public ArtifactoryRepoPath getRepoPath()
    {
        return repoPath;
    }

    public ArtifactoryUser getUser()
    {
        return user;
    }

    public Set<ArtifactoryPermission> getPermissions()
    {
        return permissions;
    }

    public void addPermission( ArtifactoryPermission permission )
    {
        permissions.add( permission );
    }
    
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof ArtifactoryAcl ) )
        {
            return false;
        }

        ArtifactoryAcl acl = (ArtifactoryAcl) obj;

        return this.user.equals( acl.user ) && this.repoPath.equals( acl.repoPath )
            && this.permissions.equals( acl.permissions );
    }

}
