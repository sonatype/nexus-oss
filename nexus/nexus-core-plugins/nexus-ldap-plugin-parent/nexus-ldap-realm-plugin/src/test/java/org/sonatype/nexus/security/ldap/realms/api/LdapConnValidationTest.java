/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import junit.framework.Assert;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;


public class LdapConnValidationTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapConnectionInfoPlexusResource" );
    }

    private LdapConnectionInfoDTO getPopulatedDTO()
    {
        LdapConnectionInfoDTO connectionInfo = new LdapConnectionInfoDTO();
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( 12345 );
        connectionInfo.setSearchBase( "o=sonatype" );
        connectionInfo.setSystemPassword( "secret" );
        connectionInfo.setSystemUsername( "uid=admin,ou=system" );
        connectionInfo.setProtocol( "ldap" );
        connectionInfo.setAuthScheme( "simple" );
        return connectionInfo;
    }

    public void testNoHost()
        throws Exception
    {
        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = this.getPopulatedDTO();
        response.setData( connectionInfo );

        connectionInfo.setHost( null );

        try
        {
            resource.put( null, null, null, response );
            Assert.fail( "Expected PlexusResourceException" );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse result = (ErrorResponse) e.getResultObject();
            Assert.assertEquals( 1, result.getErrors().size() );
            Assert.assertTrue( "Expected error to have the work 'host', was: " + this.getErrorString( result, 0 ),
                               ( this.getErrorString( result, 0 ).toString().toLowerCase().contains( "host" ) ) );
        }

    }

    public void testMultipleErrors()
        throws Exception
    {
        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = this.getPopulatedDTO();
        response.setData( connectionInfo );

        connectionInfo.setHost( null );
        connectionInfo.setPort( 0 );
        try
        {
            resource.put( null, null, null, response );
            Assert.fail( "Expected PlexusResourceException" );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse result = (ErrorResponse) e.getResultObject();
            Assert.assertEquals( 2, result.getErrors().size() );
        }
    }
}
