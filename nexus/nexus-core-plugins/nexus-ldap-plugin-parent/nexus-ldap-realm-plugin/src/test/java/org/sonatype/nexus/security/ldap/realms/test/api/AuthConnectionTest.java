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
package org.sonatype.nexus.security.ldap.realms.test.api;

import junit.framework.Assert;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapAuthenticationTestRequest;
import org.sonatype.plexus.rest.resource.PlexusResource;


public class AuthConnectionTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapTestAuthenticationPlexusResource" );
    }

    public void testSuccess()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setAuthScheme( "none" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );
        // dto.setSystemUsername( systemUsername );
        // dto.setSystemPassword( systemPassword );

        Request request = new Request();
        Response response = new Response( request );

        Assert.assertNull( resource.put( null, request, response, testRequest ) );
        Assert.assertEquals( 204, response.getStatus().getCode() );
    }

    public void testSimpleSchemaWithNoPassFailure()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setAuthScheme( "simple" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );
        // dto.setSystemUsername( systemUsername );
        // dto.setSystemPassword( systemPassword );

        Request request = new Request();
        Response response = new Response( request );

        try
        {
            resource.put( null, request, response, testRequest );
            Assert.fail( "Expected ResourceException" );
        }
        catch ( ResourceException e )
        {
            Assert.assertEquals( 400, e.getStatus().getCode() );
        }
    }

    // public void testSuccessSonatype() throws Exception
    // {
    // LdapTestAuthenticationPlexusResource resource = getResource();
    // LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
    // LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
    // testRequest.setData( dto );
    // dto.setAuthScheme( "none" );
    // dto.setHost( "ldap.sonatype.com" );
    // dto.setPort( 636 );
    // dto.setProtocol( "ldaps" );
    // dto.setSearchBase( "dc=sonatype,dc=com" );
    // dto.setSystemUsername( null );
    // dto.setSystemPassword( null );
    //
    // Request request = new Request();
    // Response response = new Response(request);
    //
    // Assert.assertNull( resource.put( null, request, response, testRequest ) );
    // Assert.assertEquals( 204, response.getStatus().getCode() );
    // }

    public void testSuccessWithPass()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setAuthScheme( "simple" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );
        dto.setSystemUsername( "uid=admin,ou=system" );
        dto.setSystemPassword( "secret" );

        Request request = new Request();
        Response response = new Response( request );

        Assert.assertNull( resource.put( null, request, response, testRequest ) );
        Assert.assertEquals( 204, response.getStatus().getCode() );
    }

    public void testFailure()
        throws Exception
    {

        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setAuthScheme( "none" );
        dto.setHost( "localhost" );
        dto.setPort( 12346 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );
        // dto.setSystemUsername( systemUsername );
        // dto.setSystemPassword( systemPassword );

        Request request = new Request();
        Response response = new Response( request );

        try
        {
            resource.put( null, request, response, testRequest );
            Assert.fail( "Expected ResourceException" );
        }
        catch ( ResourceException e )
        {
            Assert.assertEquals( 400, e.getStatus().getCode() );
        }
    }

    public void testFailureWrongPass()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setAuthScheme( "simple" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );
        dto.setSystemUsername( "uid=admin,ou=system" );
        dto.setSystemPassword( "JUNK" );

        Request request = new Request();
        Response response = new Response( request );

        try
        {
            resource.put( null, request, response, testRequest );
            Assert.fail( "Expected ResourceException" );
        }
        catch ( ResourceException e )
        {
            Assert.assertEquals( 400, e.getStatus().getCode() );
        }
    }

}
