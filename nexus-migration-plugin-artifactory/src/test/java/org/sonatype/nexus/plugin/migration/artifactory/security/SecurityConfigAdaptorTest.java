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

public class SecurityConfigAdaptorTest
{

    protected SecurityConfigAdaptor configAdaptor;

    @Before
    public void prepare()
    {
        // initialize security config
        ArtifactorySecurityConfig config = new ArtifactorySecurityConfig();

        // users
        ArtifactoryUser admin = new ArtifactoryUser( "arti-admin" );
        admin.setAdmin( true );
        ArtifactoryUser user = new ArtifactoryUser( "arti-user" );
        config.addUser( admin );
        config.addUser( user );

        // repoPaths
        ArtifactoryRepoPath apache = new ArtifactoryRepoPath( "apache", "org/apache" );
        ArtifactoryRepoPath jvnet = new ArtifactoryRepoPath( "jvnet", "ANY" );
        config.addRepoPath( apache );
        config.addRepoPath( jvnet );

        // acls
        ArtifactoryAcl apacheAcl = new ArtifactoryAcl( apache, user );
        apacheAcl.addPermission( ArtifactoryPermission.READER );
        ArtifactoryAcl jvnetAcl = new ArtifactoryAcl( jvnet, user );
        jvnetAcl.addPermission( ArtifactoryPermission.ADMIN );
        jvnetAcl.addPermission( ArtifactoryPermission.DEPLOYER );
        jvnetAcl.addPermission( ArtifactoryPermission.READER );
        config.addAcl( jvnetAcl );
        config.addAcl( apacheAcl );

        configAdaptor = new SecurityConfigAdaptor( config, new FakePersistor() );

        configAdaptor.convert();
    }

    @Test
    public void assertRepositoryTarget()
    {
        List<CRepositoryTarget> repoTargets = configAdaptor.getRepositoryTargets();

        CRepositoryTarget targetApache = repoTargets.get( 0 );
        Assert.assertNotNull( targetApache.getId() );
        Assert.assertEquals( "apache", targetApache.getName() );
        Assert.assertEquals( "maven2", targetApache.getContentClass() );
        Assert.assertEquals( 1, targetApache.getPatterns().size() );
        Assert.assertTrue( targetApache.getPatterns().contains( "org/apache/.*" ) );

        CRepositoryTarget targetJvnet = repoTargets.get( 1 );
        Assert.assertNotNull( targetJvnet.getId() );
        Assert.assertEquals( "jvnet", targetJvnet.getName() );
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
        Assert.assertEquals( "apache - (create)", privileges.get( 0 ).getName() );
        Assert.assertEquals( "apache - (create) imported from Artifactory", privileges.get( 0 ).getDescription() );
        Assert.assertEquals( "target", privileges.get( 0 ).getType() );

        Assert.assertEquals( "method", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 0 ) ).getKey() );
        Assert.assertEquals( "create", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 0 ) ).getValue() );
        Assert.assertEquals( "repositoryTargetId", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 1 ) )
            .getKey() );
        Assert.assertNotNull( ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 1 ) ).getValue() );
        Assert.assertEquals( "repositoryId", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 2 ) )
            .getKey() );
        Assert.assertEquals( "apache", ( (SecurityProperty) privileges.get( 0 ).getProperties().get( 2 ) ).getValue() );

        Assert.assertNotNull( privileges.get( 7 ).getId() );
        Assert.assertEquals( "jvnet - (delete)", privileges.get( 7 ).getName() );
        Assert.assertEquals( "jvnet - (delete) imported from Artifactory", privileges.get( 7 ).getDescription() );
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

        Assert.assertEquals( "repo-apache-read", roles.get( 0 ).getId() );
        Assert.assertEquals( "Repo: apache (read)", roles.get( 0 ).getName() );
        Assert.assertEquals( 60, roles.get( 0 ).getSessionTimeout() );
        Assert.assertEquals( 1, roles.get( 0 ).getPrivileges().size() );

        Assert.assertEquals( "repo-jvnet-all", roles.get( 3 ).getId() );
        Assert.assertEquals( "Repo: jvnet (all)", roles.get( 3 ).getName() );
        Assert.assertEquals( 60, roles.get( 3 ).getSessionTimeout() );
        Assert.assertEquals( 4, roles.get( 3 ).getPrivileges().size() );
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

        Assert.assertFalse( user.getRoles().contains( "admin" ) );
        Assert.assertTrue( user.getRoles().contains( "repo-apache-read" ) );
        Assert.assertTrue( user.getRoles().contains( "repo-jvnet-all" ) );
    }

    class FakePersistor
        implements SecurityConfigAdaptorPersistor
    {
        private int repoTargetIdCount = 1001;

        private int privilegeIdCount = 2001;

        public void persistRepositoryTarget( CRepositoryTarget repoTarget )
        {
            repoTarget.setId( "repoTarget-" + repoTargetIdCount );
            repoTargetIdCount++;
        }

        public void persistSecurityPrivilege( SecurityPrivilege privilege )
        {
            privilege.setId( "privilege-" + privilegeIdCount );
            privilegeIdCount++;
        }

        public void persistSecurityRole( SecurityRole role )
        {

        }

        public void persistSecurityUser( SecurityUser user )
        {

        }

    }
}
