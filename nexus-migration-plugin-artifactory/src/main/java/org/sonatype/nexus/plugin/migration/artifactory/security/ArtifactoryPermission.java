package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.HashSet;
import java.util.Set;

public enum ArtifactoryPermission
{
    ADMIN, DEPLOYER, READER, DELETE;

    public static Set<ArtifactoryPermission> buildPermission125( int mask )
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
    
    public static Set<ArtifactoryPermission> buildPermission130( int mask )
    {
        Set<ArtifactoryPermission> permissions = new HashSet<ArtifactoryPermission>();
        
        if ( ( mask & 1 ) == 1 )
        {
            permissions.add( READER );
        }

        if ( ( mask & 2 ) == 2 )
        {
            permissions.add( DEPLOYER );
        }

        if ( ( mask & 8 ) == 8 )
        {
            permissions.add( DELETE );
        }

        if ( ( mask & 16 ) == 16 )
        {
            permissions.add( ADMIN );
        }
        return permissions;
    }
}
