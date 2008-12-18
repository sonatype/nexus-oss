package org.sonatype.nexus.plugin.migration.artifactory.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugin.migration.artifactory.security.builder.ArtifactorySecurityConfigBuilder;

public class ParseSecurityConfig130Test
{
    protected ArtifactorySecurityConfig securityConfig;

    @Before
    public void parseSecurityConfig()
        throws Exception
    {
        // Note that the whole test case is based on this configuration file
        securityConfig = ArtifactorySecurityConfigBuilder.read( getClass().getResourceAsStream(
            "/security-config-1.3.0.xml" ) );
    }

    @Test
    public void assertUser()
    {
        ArtifactoryUser anonymous = new ArtifactoryUser( "anonymous" );
        ArtifactoryUser admin = new ArtifactoryUser( "admin" );
        admin.setAdmin( true );
        ArtifactoryUser admin1 = new ArtifactoryUser( "admin1", "admin1@artifactory.org" );
        admin1.setAdmin( true );
        ArtifactoryUser user = new ArtifactoryUser( "user", "user@artifactory.org" );
        ArtifactoryUser user1 = new ArtifactoryUser( "user1", "user1@artifactory.org" );

        List<ArtifactoryUser> users = new ArrayList<ArtifactoryUser>();

        users.add( anonymous );
        users.add( admin );
        users.add( admin1 );
        users.add( user );
        users.add( user1 );

        Assert.assertEquals( users, securityConfig.getUsers() );
    }
}
