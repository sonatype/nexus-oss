package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.List;

public class ArtifactorySecurityConfig
{
    private List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();

    private List<ArtifactoryRepoPath> repoPaths = new ArrayList<ArtifactoryRepoPath>();

    private List<ArtifactoryAcl> acls = new ArrayList<ArtifactoryAcl>();

    public List<ArtifactoryUser> getUsers()
    {
        return users;
    }

    public List<ArtifactoryRepoPath> getRepoPaths()
    {
        return repoPaths;
    }

    public List<ArtifactoryAcl> getAcls()
    {
        return acls;
    }

    public void addUser( ArtifactoryUser user )
    {
        users.add( user );
    }

    public void addRepoPath( ArtifactoryRepoPath repoPath )
    {
        repoPaths.add( repoPath );
    }

    public void addAcl( ArtifactoryAcl acl )
    {
        acls.add( acl );
    }

}
