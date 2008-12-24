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
