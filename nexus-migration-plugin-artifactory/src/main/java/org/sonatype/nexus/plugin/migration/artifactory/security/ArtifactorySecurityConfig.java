package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.List;

public class ArtifactorySecurityConfig
{
    private List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();

    private List<ArtifactoryGroup> groups = new ArrayList<ArtifactoryGroup>();

    private List<ArtifactoryPermissionTarget> permissionTargets = new ArrayList<ArtifactoryPermissionTarget>();

    private List<ArtifactoryAcl> acls = new ArrayList<ArtifactoryAcl>();

    public List<ArtifactoryUser> getUsers()
    {
        return users;
    }

    public List<ArtifactoryGroup> getGroups()
    {
        return groups;
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

    public void addGroup( ArtifactoryGroup group )
    {
        groups.add( group );
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
    
    public ArtifactoryGroup getGroupByName( String name )
    {
        for ( ArtifactoryGroup group : groups )
        {
            if ( group.getName().equals( name ) )
            {
                return group;
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
    
    
    public ArtifactoryPermissionTarget getPermissionTarget( String name )
    {
        for ( ArtifactoryPermissionTarget target : permissionTargets )
        {
            if ( target.getId().equals( name ) )
            {
                return target;
            }
        }
        return null;
    }

}
