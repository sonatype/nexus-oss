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

import org.junit.Before;
import org.junit.Test;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.plugin.migration.artifactory.MigrationResult;
import org.sonatype.nexus.plugin.migration.artifactory.persist.MappingConfiguration;
import org.sonatype.nexus.plugin.migration.artifactory.persist.model.CMapping;

public class SecurityConfigConvertorTest
{

    protected SecurityConfigConvertor configConvertor;

    protected List<CRepositoryTarget> repoTargetList;

    protected List<SecurityPrivilege> privList;

    protected List<SecurityRole> roleList;

    protected List<SecurityUser> userList;

    @Before
    public void prepare()
        throws Exception
    {
        // initialize security config
        ArtifactorySecurityConfig config = new ArtifactorySecurityConfig();

        repoTargetList = new ArrayList<CRepositoryTarget>();
        privList = new ArrayList<SecurityPrivilege>();
        roleList = new ArrayList<SecurityRole>();
        userList = new ArrayList<SecurityUser>();

        // groups
        ArtifactoryGroup group = new ArtifactoryGroup( "group", "test" );
        config.addGroup( group );

        // users
        ArtifactoryUser admin = new ArtifactoryUser( "arti-admin", "5f4dcc3b5aa765d61d8327deb882cf99" );
        admin.setAdmin( true );
        ArtifactoryUser user = new ArtifactoryUser( "arti-user", "5f4dcc3b5aa765d61d8327deb882cf99" );
        user.getGroups().add( group );
        ArtifactoryUser user1 = new ArtifactoryUser( "arti-user1", "5f4dcc3b5aa765d61d8327deb882cf99" );
        config.addUser( admin );
        config.addUser( user );
        config.addUser( user1 );

        // repoPaths
        ArtifactoryPermissionTarget apache = new ArtifactoryPermissionTarget( "apachePermTarget", "apache" );
        apache.addInclude( "org/apache/.*" );
        ArtifactoryPermissionTarget jvnet = new ArtifactoryPermissionTarget( "jvnet" );
        jvnet.addInclude( ".*" );
        config.addPermissionTarget( apache );
        config.addPermissionTarget( jvnet );

        // acls
        ArtifactoryAcl apacheAcl = new ArtifactoryAcl( apache, user );
        apacheAcl.addPermission( ArtifactoryPermission.READER );
        ArtifactoryAcl jvnetAcl = new ArtifactoryAcl( jvnet, user );
        jvnetAcl.addPermission( ArtifactoryPermission.ADMIN );
        jvnetAcl.addPermission( ArtifactoryPermission.DEPLOYER );
        jvnetAcl.addPermission( ArtifactoryPermission.READER );
        config.addAcl( jvnetAcl );
        config.addAcl( apacheAcl );

        ArtifactoryAcl apacheAcl2 = new ArtifactoryAcl( apache, user1 );
        apacheAcl2.addPermission( ArtifactoryPermission.READER );
        apacheAcl2.addPermission( ArtifactoryPermission.DEPLOYER );
        config.addAcl( apacheAcl2 );

        ArtifactoryAcl groupAcl = new ArtifactoryAcl( jvnet, group );
        groupAcl.addPermission( ArtifactoryPermission.DELETE );
        groupAcl.addPermission( ArtifactoryPermission.READER );
        config.addAcl( groupAcl );

        configConvertor = new SecurityConfigConvertor( config, new FakeReceiver(), new FakeMappingConfiguration(), new MockMigrationResults() );
    }

    @Test
    public void assertRepositoryTarget()
        throws Exception
    {
        configConvertor.convert();

        CRepositoryTarget targetApache = repoTargetList.get( 0 );
        Assert.assertEquals( "apachePermTarget", targetApache.getId() );
        Assert.assertEquals( "apachePermTarget", targetApache.getName() );
        Assert.assertEquals( "maven2", targetApache.getContentClass() );
        Assert.assertEquals( 1, targetApache.getPatterns().size() );
        Assert.assertTrue( targetApache.getPatterns().contains( "org/apache/.*" ) );

        CRepositoryTarget targetJvnet = repoTargetList.get( 1 );
        Assert.assertNotNull( targetJvnet.getId() );
        Assert.assertNotNull( targetJvnet.getName() );
        Assert.assertEquals( "maven2", targetJvnet.getContentClass() );
        Assert.assertEquals( 1, targetJvnet.getPatterns().size() );
        Assert.assertTrue( targetJvnet.getPatterns().contains( ".*" ) );
    }

    @Test
    public void assertPrivilege()
        throws Exception
    {
        configConvertor.convert();

        // assert the privileges
        Assert.assertNotNull( privList.get( 0 ).getId() );
        Assert.assertEquals( "apachePermTarget-create", privList.get( 0 ).getName() );
        Assert.assertEquals( "apachePermTarget-create", privList.get( 0 ).getDescription() );
        Assert.assertEquals( "target", privList.get( 0 ).getType() );

        Assert.assertEquals( "method", ( (SecurityProperty) privList.get( 0 ).getProperties().get( 0 ) ).getKey() );
        Assert.assertEquals( "create", ( (SecurityProperty) privList.get( 0 ).getProperties().get( 0 ) ).getValue() );
        Assert.assertEquals( "repositoryTargetId", ( (SecurityProperty) privList.get( 0 ).getProperties().get( 1 ) )
            .getKey() );
        Assert.assertEquals( "apachePermTarget", ( (SecurityProperty) privList.get( 0 ).getProperties().get( 1 ) )
            .getValue() );
        Assert
            .assertEquals( "repositoryId", ( (SecurityProperty) privList.get( 0 ).getProperties().get( 2 ) ).getKey() );
        Assert.assertEquals( "apache", ( (SecurityProperty) privList.get( 0 ).getProperties().get( 2 ) ).getValue() );

        Assert.assertNotNull( privList.get( 7 ).getId() );
        Assert.assertTrue( privList.get( 7 ).getName().endsWith( "delete" ) );
        Assert.assertEquals( privList.get( 7 ).getDescription(), privList.get( 7 ).getName() );
        Assert.assertEquals( "target", privList.get( 7 ).getType() );

        Assert.assertEquals( "method", ( (SecurityProperty) privList.get( 7 ).getProperties().get( 0 ) ).getKey() );
        Assert.assertEquals( "delete", ( (SecurityProperty) privList.get( 7 ).getProperties().get( 0 ) ).getValue() );
        Assert.assertEquals( "repositoryTargetId", ( (SecurityProperty) privList.get( 7 ).getProperties().get( 1 ) )
            .getKey() );
        Assert.assertNotNull( ( (SecurityProperty) privList.get( 7 ).getProperties().get( 1 ) ).getValue() );
        Assert
            .assertEquals( "repositoryId", ( (SecurityProperty) privList.get( 7 ).getProperties().get( 2 ) ).getKey() );
        Assert.assertEquals( "jvnet", ( (SecurityProperty) privList.get( 7 ).getProperties().get( 2 ) ).getValue() );
    }

    @Test
    public void assertRole()
        throws Exception
    {
        configConvertor.convert();

        Assert.assertEquals( 9, roleList.size() );

        // the READER, DEPLOYER, DELETE, ADMIN by order for each target
        Assert.assertEquals( "apachePermTarget-reader", roleList.get( 0 ).getId() );
        Assert.assertEquals( "apachePermTarget-reader", roleList.get( 0 ).getName() );
        Assert.assertEquals( 60, roleList.get( 0 ).getSessionTimeout() );
        // read priv
        Assert.assertEquals( 1, roleList.get( 0 ).getPrivileges().size() );

        Assert.assertTrue( roleList.get( 6 ).getId().endsWith( "delete" ) );
        Assert.assertEquals( roleList.get( 6 ).getId(), roleList.get( 6 ).getName() );
        Assert.assertEquals( 60, roleList.get( 6 ).getSessionTimeout() );
        // delete priv
        Assert.assertEquals( 1, roleList.get( 6 ).getPrivileges().size() );

        Assert.assertTrue( roleList.get( 7 ).getId().endsWith( "admin" ) );
        Assert.assertEquals( roleList.get( 7 ).getId(), roleList.get( 7 ).getName() );
        Assert.assertEquals( 60, roleList.get( 7 ).getSessionTimeout() );
        // update and delete priv
        Assert.assertEquals( 2, roleList.get( 7 ).getPrivileges().size() );

        // groups will be last received as roles
        Assert.assertEquals( "group", roleList.get( 8 ).getId() );
    }

    @Test
    public void assertAdmin()
        throws Exception
    {
        configConvertor.convert();

        SecurityUser admin = userList.get( 0 );

        Assert.assertEquals( "arti-admin", admin.getId() );
        Assert.assertEquals( "5f4dcc3b5aa765d61d8327deb882cf99", admin.getPassword() );
        Assert.assertEquals( "arti-admin", admin.getName() );
        Assert.assertEquals( "changeme@yourcompany.com", admin.getEmail() );
        Assert.assertEquals( "active", admin.getStatus() );

        Assert.assertTrue( admin.getRoles().contains( "admin" ) );
    }

    @Test
    public void assertUser()
        throws Exception
    {
        configConvertor.convert();

        SecurityUser user = userList.get( 1 );

        Assert.assertEquals( "arti-user", user.getId() );
        Assert.assertEquals( "arti-user", user.getName() );
        Assert.assertEquals("5f4dcc3b5aa765d61d8327deb882cf99", user.getPassword());
        Assert.assertEquals( "changeme@yourcompany.com", user.getEmail() );
        Assert.assertEquals( "active", user.getStatus() );

        Assert.assertEquals( 5, user.getRoles().size() );
        Assert.assertFalse( user.getRoles().contains( "apachePermTarget-admin" ) );
        Assert.assertTrue( user.getRoles().contains( "apachePermTarget-reader" ) );
        Assert.assertTrue( user.getRoles().contains( roleList.get( 5 ).getId() ) );
        // for group
        Assert.assertTrue( user.getRoles().contains( "group" ) );

        SecurityUser user1 = userList.get( 2 );
        Assert.assertEquals( 2, user1.getRoles().size() );
    }

    @Test
    public void disableResolvePermission()
        throws Exception
    {
        configConvertor.setResolvePermission( false );
        configConvertor.convert();

        Assert.assertEquals( 3, userList.size() );
        Assert.assertEquals( 1, roleList.size() );
        Assert.assertEquals( 0, privList.size() );
        Assert.assertEquals( 0, repoTargetList.size() );

        Assert.assertTrue( userList.get( 0 ).getRoles().contains( "admin" ) );
        Assert.assertTrue( userList.get( 1 ).getRoles().contains( "group" ) );
        Assert.assertTrue( userList.get( 2 ).getRoles().contains( "anonymous" ) );

        Assert.assertTrue( roleList.get( 0 ).getPrivileges().isEmpty() );
        Assert.assertTrue( roleList.get( 0 ).getRoles().contains( "anonymous" ) );
    }

    class FakeReceiver
        implements SecurityConfigReceiver
    {
        private int privilegeIdCount = 2001;

        public void receiveRepositoryTarget( CRepositoryTarget repoTarget )
        {
            repoTargetList.add( repoTarget );
        }

        public void receiveSecurityPrivilege( SecurityPrivilege privilege )
        {
            privilege.setId( "privilege-" + privilegeIdCount );
            privilegeIdCount++;

            privList.add( privilege );
        }

        public void receiveSecurityRole( SecurityRole role )
        {
            roleList.add( role );
        }

        public void receiveSecurityUser( SecurityUser user )
        {
            userList.add( user );
        }
    }

    class FakeMappingConfiguration
        implements MappingConfiguration
    {

        public void addMapping( CMapping map )
        {
            // nothing
        }

        // always return the same repository id
        public CMapping getMapping( String repositoryId )
        {
            CMapping mapping = new CMapping();

            mapping.setArtifactoryRepositoryId( repositoryId );

            mapping.setNexusGroupId( null );

            mapping.setNexusRepositoryId( repositoryId );

            return mapping;
        }

        public void save()
        {
            // nothing
        }

    }
    
    class MockMigrationResults implements MigrationResult
    {

        public void addErrorMessage( String errorMessage )
        {
            // TODO Auto-generated method stub
            
        }

        public void addWarningMessage( String warningMessage )
        {
            // TODO Auto-generated method stub
            
        }

        public void clear()
        {
            // TODO Auto-generated method stub
            
        }

        public List<String> getErrorMessages()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public List<String> getWarningMessages()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public void mergeResult( MigrationResult migrationResult )
        {
            // TODO Auto-generated method stub
            
        }
        
    }

}
