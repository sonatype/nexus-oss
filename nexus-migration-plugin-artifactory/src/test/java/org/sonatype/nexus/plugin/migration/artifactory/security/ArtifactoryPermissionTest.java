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

import junit.framework.Assert;

import org.junit.Test;

public class ArtifactoryPermissionTest
{
    @Test
    public void buildPermission125()
    {
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 7 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 7 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 7 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 6 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 6 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 6 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 5 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 5 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 5 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 4 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 4 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 4 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 3 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 3 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 3 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 2 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 2 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 2 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertTrue( ArtifactoryPermission.buildPermission125( 1 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 1 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 1 ).contains( ArtifactoryPermission.READER ) );

        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 0 ).contains( ArtifactoryPermission.ADMIN ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 0 ).contains( ArtifactoryPermission.DEPLOYER ) );
        Assert.assertFalse( ArtifactoryPermission.buildPermission125( 0 ).contains( ArtifactoryPermission.READER ) );
    }
}
