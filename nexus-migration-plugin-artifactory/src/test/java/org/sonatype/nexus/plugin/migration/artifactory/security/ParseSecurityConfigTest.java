package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ParseSecurityConfigTest
{
    
    protected ArtifactorySecurityConfig securityConfig;
    
    @Before
    public void parseSecurityConfig()
        throws Exception
    {
        securityConfig = ArtifactorySecurityConfig
            .read( getClass().getResourceAsStream( "/security-config-1.2.5.xml" ) );
    }
    
    @Test
    public void assertUser()
    {
        ArtifactoryUser admin = new ArtifactoryUser("admin", "5f4dcc3b5aa765d61d8327deb882cf99");
        admin.addRole( ArtifactoryRole.ADMIN );
        admin.addRole( ArtifactoryRole.USER );
        
        ArtifactoryUser admin1 = new ArtifactoryUser("admin1", "5f4dcc3b5aa765d61d8327deb882cf99");
        admin1.addRole( ArtifactoryRole.ADMIN );
        admin1.addRole( ArtifactoryRole.USER );   
        
        ArtifactoryUser user = new ArtifactoryUser("user", "5f4dcc3b5aa765d61d8327deb882cf99");
        user.addRole( ArtifactoryRole.USER ); 
        
        ArtifactoryUser user1 = new ArtifactoryUser("user1", "5f4dcc3b5aa765d61d8327deb882cf99");
        user1.addRole( ArtifactoryRole.USER );  
        
        List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();
        
        users.add( admin );
        users.add( admin1 );
        users.add( user );
        users.add( user1 );
        
        Assert.assertEquals( users, securityConfig.getUsers() );
    }
    
    @Test
    public void assertRepoPath()
    {
        ArtifactoryRepoPath repoPath1 = new ArtifactoryRepoPath(ArtifactoryRepoPath.REPO_KEY_ANY, ArtifactoryRepoPath.PATH_ANY);
        ArtifactoryRepoPath repoPath2 = new ArtifactoryRepoPath("libs-releases", "org/apache");
        ArtifactoryRepoPath repoPath3 = new ArtifactoryRepoPath("java.net-cache", ArtifactoryRepoPath.PATH_ANY);
    
        List<ArtifactoryRepoPath> repoPaths = new ArrayList<ArtifactoryRepoPath>();
        
        repoPaths.add( repoPath1 );
        repoPaths.add( repoPath2 );
        repoPaths.add( repoPath3 );
        
        Assert.assertEquals( repoPaths, securityConfig.getRepoPaths() );
    }
}
