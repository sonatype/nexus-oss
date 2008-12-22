package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.jsecurity.realms.tools.dao.SecurityPrivilege;
import org.sonatype.jsecurity.realms.tools.dao.SecurityProperty;
import org.sonatype.jsecurity.realms.tools.dao.SecurityRole;
import org.sonatype.jsecurity.realms.tools.dao.SecurityUser;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;

public class SecurityConfigConvertorTest
{

    protected SecurityConfigConvertor configAdaptor;

    @Before
    public void prepare()
        throws Exception
    {
        // initialize security config
        ArtifactorySecurityConfig config = new ArtifactorySecurityConfig();

        // users
        ArtifactoryUser admin = new ArtifactoryUser( "arti-admin" );
        admin.setAdmin( true );
        ArtifactoryUser user = new ArtifactoryUser( "arti-user" );
        config.addUser( admin );
        config.addUser( user );
        ArtifactoryUser user1 = new ArtifactoryUser( "arti-user1" );
        config.addUser( user1 );

        // repoPaths
        ArtifactoryPermissionTarget apache = new ArtifactoryPermissionTarget( "apachePermTarget", "apache" );
        apache.addInclude( "org/apache" );
        ArtifactoryPermissionTarget jvnet = new ArtifactoryPermissionTarget( "jvnet" );
        jvnet.addInclude( "ANY" );
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

        configAdaptor = new SecurityConfigConvertor( config, new FakeReceiver() );

        configAdaptor.convert();
    }

    @Test
    public void assertRepositoryTarget()
    {
        List<CRepositoryTarget> repoTargets = configAdaptor.getRepositoryTargets();

        CRepositoryTarget targetApache = repoTargets.get( 0 );
        Assert.assertEquals( "apachePermTarget", targetApache.getId() );
        Assert.assertEquals( "apachePermTarget", targetApache.getName() );
        Assert.assertEquals( "maven2", targetApache.getContentClass() );
        Assert.assertEquals( 1, targetApache.getPatterns().size() );
        Assert.assertTrue( targetApache.getPatterns().contains( "org/apache/.*" ) );

        CRepositoryTarget targetJvnet = repoTargets.get( 1 );
        Assert.assertNotNull( targetJvnet.getId() );
        Assert.assertNotNull( targetJvnet.getName() );
        Assert.assertEquals( "maven2", targetJvnet.getContentClass() );
        Assert.assertEquals( 1, targetJvnet.getPatterns().size() );
        Assert.assertTrue( targetJvnet.getPatterns().contains( ".*" ) );
    }

    @Test
    public void assertPrivilege()
    {
        // assert the privileges
        List<SecurityPrivilege> privileges = configAdaptor.getSecurityPrivileges();

        Assert.assertNotNull( privileges.get( 0 ).getId() );
        Assert.assertEquals( "apachePermTarget-create", privileges.get( 0 ).getName() );
        Assert.assertEquals( "apachePermTarget-create", privileges.get( 0 ).getDescription() );
        Assert.assertEquals( "target", privileges.get( 0 ).getType() );

        Assert.assertEquals( "method", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 0 ) ).getKey() );
        Assert.assertEquals( "create", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 0 ) ).getValue() );
        Assert.assertEquals( "repositoryTargetId", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 1 ) )
            .getKey() );
        Assert.assertEquals( "apachePermTarget", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 1 ) )
            .getValue() );
        Assert.assertEquals( "repositoryId", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 2 ) )
            .getKey() );
        Assert.assertEquals( "apache", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 2 ) ).getValue() );

        Assert.assertNotNull( privileges.get( 7 ).getId() );
        Assert.assertTrue( privileges.get( 7 ).getName().endsWith( "delete" ) );
        Assert.assertEquals( privileges.get( 7 ).getDescription(), privileges.get( 7 ).getName() );
        Assert.assertEquals( "target", privileges.get( 7 ).getType() );

        Assert.assertEquals( "method", ( (SecurityProperty) privileges.get( 7 ).getProperties().get( 0 ) ).getKey() );
        Assert.assertEquals( "delete", ( (SecurityProperty) privileges.get( 7 ).getProperties().get( 0 ) ).getValue() );
        Assert.assertEquals( "repositoryTargetId", ( (SecurityProperty) privileges.get( 7 ).getProperties().get( 1 ) )
            .getKey() );
        Assert.assertNotNull( ( (SecurityProperty) privileges.get( 7 ).getProperties().get( 1 ) ).getValue() );
        Assert.assertEquals( "repositoryId", ( (SecurityProperty) privileges.get( 7 ).getProperties().get( 2 ) )
            .getKey() );
        Assert.assertEquals( "jvnet", ( (SecurityProperty) privileges.get( 7 ).getProperties().get( 2 ) ).getValue() );
    }

    @Test
    public void assertRole()
    {
        List<SecurityRole> roles = configAdaptor.getSecurityRoles();

        Assert.assertEquals( "apachePermTarget-reader", roles.get( 0 ).getId() );
        Assert.assertEquals( "apachePermTarget-reader", roles.get( 0 ).getName() );
        Assert.assertEquals( 60, roles.get( 0 ).getSessionTimeout() );
        // read priv
        Assert.assertEquals( 1, roles.get( 0 ).getPrivileges().size() );

        Assert.assertTrue( roles.get( 5 ).getId().endsWith( "admin" ) );
        Assert.assertEquals( roles.get( 5 ).getId(), roles.get( 5 ).getName() );
        Assert.assertEquals( 60, roles.get( 5 ).getSessionTimeout() );
        // update and delete priv
        Assert.assertEquals( 2, roles.get( 5 ).getPrivileges().size() );
    }

    @Test
    public void assertAdmin()
    {
        List<SecurityUser> users = configAdaptor.getSecurityUsers();

        SecurityUser admin = users.get( 0 );

        Assert.assertEquals( "arti-admin", admin.getId() );
        Assert.assertEquals( "arti-admin", admin.getName() );
        Assert.assertTrue( StringUtils.isEmpty( admin.getEmail() ) );
        Assert.assertEquals( "active", admin.getStatus() );

        Assert.assertTrue( admin.getRoles().contains( "admin" ) );
    }

    @Test
    public void assertUser()
    {
        List<SecurityUser> users = configAdaptor.getSecurityUsers();

        SecurityUser user = users.get( 1 );

        Assert.assertEquals( "arti-user", user.getId() );
        Assert.assertEquals( "arti-user", user.getName() );
        Assert.assertTrue( StringUtils.isEmpty( user.getEmail() ) );
        Assert.assertEquals( "active", user.getStatus() );

        Assert.assertEquals( 4, user.getRoles().size() );
        Assert.assertFalse( user.getRoles().contains( "apachePermTarget-admin" ) );
        Assert.assertTrue( user.getRoles().contains( "apachePermTarget-reader" ) );
        Assert.assertTrue( user.getRoles().contains( configAdaptor.getSecurityRoles().get( 5 ).getId() ) );

        SecurityUser user1 = users.get( 2 );
        Assert.assertEquals( 2, user1.getRoles().size() );
    }

    class FakeReceiver
        implements SecurityConfigReceiver
    {
        private int privilegeIdCount = 2001;

        public void receiveRepositoryTarget( CRepositoryTarget repoTarget )
        {
        }

        public void receiveSecurityPrivilege( SecurityPrivilege privilege )
        {
            privilege.setId( "privilege-" + privilegeIdCount );
            privilegeIdCount++;
        }

        public void receiveSecurityRole( SecurityRole role )
        {

        }

        public void receiveSecurityUser( SecurityUser user )
        {

        }

    }
}
