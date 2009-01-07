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
