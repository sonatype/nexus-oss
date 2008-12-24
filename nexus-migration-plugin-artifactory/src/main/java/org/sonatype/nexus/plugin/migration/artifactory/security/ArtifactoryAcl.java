package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashSet;
import java.util.Set;

public class ArtifactoryAcl
{
    private ArtifactoryPermissionTarget permissionTarget;

    private ArtifactoryUser user;

    private ArtifactoryGroup group;

    private Set<ArtifactoryPermission> permissions = new HashSet<ArtifactoryPermission>();

    public ArtifactoryAcl( ArtifactoryPermissionTarget permissionTarget, ArtifactoryUser user )
    {
        this.permissionTarget = permissionTarget;

        this.user = user;
    }

    public ArtifactoryAcl( ArtifactoryPermissionTarget permissionTarget, ArtifactoryGroup group )
    {
        this.permissionTarget = permissionTarget;

        this.group = group;
    }

    public ArtifactoryPermissionTarget getPermissionTarget()
    {
        return permissionTarget;
    }

    public ArtifactoryUser getUser()
    {
        return user;
    }

    public ArtifactoryGroup getGroup()
    {
        return group;
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

        return

        ( ( user == null && acl.user == null ) || ( user != null && acl.user != null && user.equals( acl.user ) ) )
            && ( group == null && acl.group == null || ( group != null && acl.group != null && group.equals( acl.group ) ) )
            && this.permissionTarget.equals( acl.permissionTarget ) && this.permissions.equals( acl.permissions );

    }

}
