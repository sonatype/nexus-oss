package org.sonatype.nexus.security;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public class SimpleAuthenticationSourceTest
    extends AbstractNexusTestCase
{
    public void test()
        throws Exception
    {
        AuthenticationSource source = (AuthenticationSource) lookup( AuthenticationSource.ROLE, "simple" );
        assertNotNull( "source is null", source );
    }
}
