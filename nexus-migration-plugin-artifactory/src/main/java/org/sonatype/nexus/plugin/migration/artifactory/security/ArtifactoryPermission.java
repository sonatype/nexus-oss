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
