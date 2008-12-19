package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.List;

public class ArtifactorySecurityConfig
{
    private List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();

    private List<ArtifactoryPermissionTarget> permissionTargets = new ArrayList<ArtifactoryPermissionTarget>();

    private List<ArtifactoryAcl> acls = new ArrayList<ArtifactoryAcl>();

    public List<ArtifactoryUser> getUsers()
    {
        return users;
    }

    public List<ArtifactoryPermissionTarget> getPermissionTargets()
    {
        return permissionTargets;
    }

    public List<ArtifactoryAcl> getAcls()
    {
        return acls;
    }

    public void addUser( ArtifactoryUser user )
    {
        users.add( user );
    }

    public void addPermissionTarget( ArtifactoryPermissionTarget repoPath )
    {
        permissionTargets.add( repoPath );
    }

    public void addAcl( ArtifactoryAcl acl )
    {
        acls.add( acl );
    }

    public ArtifactoryUser getUserByUsername( String username )
    {
        for ( ArtifactoryUser user : users )
        {
            if ( user.getUsername().equals( username ) )
            {
                return user;
            }
        }
        return null;
    }

    // this works for 1.2.5, there only one include path exists
    public ArtifactoryPermissionTarget getArtifactoryRepoTarget( String repoKey, String path )
    {
        for ( ArtifactoryPermissionTarget target : permissionTargets )
        {
            if ( target.getRepoKey().equals( repoKey ) && target.getIncludes().size() == 1
                && target.getExcludes().isEmpty() && target.getIncludes().get( 0 ).equals( path ) )
            {
                return target;
            }
        }
        return null;
    }

}
