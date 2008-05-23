/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
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
        assertNull( unknownUser );
    }
}
