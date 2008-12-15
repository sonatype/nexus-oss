package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashSet;
import java.util.Set;

public enum ArtifactoryPermission
{
    ADMIN, DEPLOYER, READER;

    public static Set<ArtifactoryPermission> buildPermission( int mask )
    {
        Set<ArtifactoryPermission> permissions = new HashSet<ArtifactoryPermission>();

        if ( ( mask & 1 ) == 1 )
        {
            permissions.add( ADMIN );
        }

        if ( ( mask & 2 ) == 2 )
        {
            permissions.add( READER );
        }

        if ( ( mask & 4 ) == 4 )
        {
            permissions.add( DEPLOYER );
        }

        return permissions;
    }
}
