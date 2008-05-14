package org.sonatype.nexus.security;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public class SimpleAuthenticationSourceTest
    extends AbstractNexusTestCase
{
    private AuthenticationSource source;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        source = (AuthenticationSource) lookup( AuthenticationSource.ROLE, "simple" );
        assertNotNull( "source is null", source );
        assertFalse( source.isAnynonymousAllowed() );
    }

    public void testAdminUser()
        throws Exception
    {
        assertTrue( source.isKnown( SimpleAuthenticationSource.ADMIN_USERNAME ) );
        assertTrue( source.hasPasswordSet( SimpleAuthenticationSource.ADMIN_USERNAME ) );
        User adminUser = source.authenticate( SimpleAuthenticationSource.ADMIN_USERNAME, "admin123" );
        assertNotNull( adminUser );
        assertEquals( SimpleAuthenticationSource.ADMIN_USERNAME, adminUser.getUsername() );
        assertFalse( adminUser.isAnonymous() );

        adminUser = source.authenticate( SimpleAuthenticationSource.ADMIN_USERNAME, "unknown" );
        assertNull( adminUser );
    }

    public void testDeploymentUser()
        throws Exception
    {
        assertTrue( source.isKnown( SimpleAuthenticationSource.DEPLOYMENT_USERNAME ) );
        assertFalse( source.hasPasswordSet( SimpleAuthenticationSource.DEPLOYMENT_USERNAME ) );
        User developmentUser = source.authenticate( SimpleAuthenticationSource.DEPLOYMENT_USERNAME, "unknown" );
        assertNotNull( developmentUser );
        assertEquals( SimpleAuthenticationSource.DEPLOYMENT_USERNAME, developmentUser.getUsername() );
        assertFalse( developmentUser.isAnonymous() );
    }

    public void testUnknownUser()
        throws Exception
    {
        assertFalse( source.isKnown( "UNKNOWN" ) );
        assertFalse( source.hasPasswordSet( "UNKNOWN" ) );
        User unknownUser = source.authenticate( "UNKNOWN", "unknown" );
        assertNotNull( unknownUser );
        assertEquals( "UNKNOWN", unknownUser.getUsername() );
        assertFalse( unknownUser.isAnonymous() );
    }
}
