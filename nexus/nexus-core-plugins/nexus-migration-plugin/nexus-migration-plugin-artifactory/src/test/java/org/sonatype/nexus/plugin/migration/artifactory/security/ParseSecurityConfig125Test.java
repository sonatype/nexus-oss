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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.security.builder.ArtifactorySecurityConfigBuilder;

public class ParseSecurityConfig125Test
{

    protected static ArtifactorySecurityConfig securityConfig;

    @BeforeClass
    public static void parseSecurityConfig()
        throws Exception
    {
        // Note that the whole test case is based on this configuration file
        securityConfig = ArtifactorySecurityConfigBuilder.read( ParseSecurityConfig125Test.class
            .getResourceAsStream( "/security-config-1.2.5.xml" ) );
    }

    @Test
    public void assertUser()
    {
        ArtifactoryUser admin = new ArtifactoryUser( "admin", "5f4dcc3b5aa765d61d8327deb882cf99" );
        admin.setAdmin( true );
        ArtifactoryUser admin1 = new ArtifactoryUser( "admin1", "5f4dcc3b5aa765d61d8327deb882cf99" );
        admin1.setAdmin( true );
        ArtifactoryUser user = new ArtifactoryUser( "user", "5f4dcc3b5aa765d61d8327deb882cf99" );
        ArtifactoryUser user1 = new ArtifactoryUser( "user1", "5f4dcc3b5aa765d61d8327deb882cf99" );

        List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();

        users.add( admin );
        users.add( admin1 );
        users.add( user );
        users.add( user1 );

        Assert.assertEquals( users, securityConfig.getUsers() );
    }

    @Test
    public void assertPermissionTarget()
    {
        ArtifactoryPermissionTarget target1 = new ArtifactoryPermissionTarget();
        target1.addRepoKey( "ANY" );
        target1.addInclude( ".*" );
        ArtifactoryPermissionTarget target2 = new ArtifactoryPermissionTarget();
        target2.addRepoKey( "libs-releases" );
        target2.addInclude( "org/apache/.*" );
        ArtifactoryPermissionTarget target3 = new ArtifactoryPermissionTarget();
        target3.addRepoKey( "java.net-cache" );
        target3.addInclude( ".*" );

        assertPermissionTargetContent( target1, securityConfig.getPermissionTargets().get( 0 ) );
        assertPermissionTargetContent( target2, securityConfig.getPermissionTargets().get( 1 ) );
        assertPermissionTargetContent( target3, securityConfig.getPermissionTargets().get( 2 ) );
    }

    @Test
    public void assertAcl()
    {
        ArtifactoryUser user = new ArtifactoryUser( "user", "5f4dcc3b5aa765d61d8327deb882cf99" );
        ArtifactoryUser user1 = new ArtifactoryUser( "user1", "5f4dcc3b5aa765d61d8327deb882cf99" );

        ArtifactoryPermissionTarget target2 = securityConfig.getPermissionTargets().get( 1 );
        ArtifactoryPermissionTarget target3 = securityConfig.getPermissionTargets().get( 2 );

        ArtifactoryAcl acl1 = new ArtifactoryAcl( target2, user1 );
        acl1.addPermission( ArtifactoryPermission.DEPLOYER );
        acl1.addPermission( ArtifactoryPermission.READER );

        ArtifactoryAcl acl2 = new ArtifactoryAcl( target3, user );
        acl2.addPermission( ArtifactoryPermission.ADMIN );
        acl2.addPermission( ArtifactoryPermission.DEPLOYER );
        acl2.addPermission( ArtifactoryPermission.READER );

        List<ArtifactoryAcl> acls = new ArrayList<ArtifactoryAcl>();
        acls.add( acl1 );
        acls.add( acl2 );

        Assert.assertEquals( acls, securityConfig.getAcls() );
    }

    private void assertPermissionTargetContent( ArtifactoryPermissionTarget expected, ArtifactoryPermissionTarget actual )
    {
        Assert.assertEquals( expected.getRepoKeys(), actual.getRepoKeys() );
        Assert.assertEquals( expected.getIncludes(), actual.getIncludes() );
        Assert.assertEquals( expected.getExcludes(), actual.getExcludes() );
    }
}
