/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api;

import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapAuthenticationTestRequest;
import org.sonatype.plexus.rest.resource.PlexusResource;


public class AuthMd5ConnectionTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
    throws Exception
{
    return this
        .lookup( PlexusResource.class, "LdapTestAuthenticationPlexusResource" );
}

    public void testSuccess() throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setSystemPassword( "secret" );
        dto.setSystemUsername( "admin" );
        dto.setProtocol( "ldap" );
        dto.setAuthScheme( "DIGEST-MD5" );
        dto.setRealm( "localhost" );

        Request request = new Request();
        Response response = new Response(request);

        Assert.assertNull( resource.put( null, request, response, testRequest ) );
        Assert.assertEquals( 204, response.getStatus().getCode() );
    }

    public void testSuccessWithPass() throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setSystemPassword( "secret" );
        dto.setSystemUsername( "uid=admin,ou=system" );
        dto.setProtocol( "ldap" );
        dto.setAuthScheme( "simple" );
        dto.setRealm( "localhost" );

        Request request = new Request();
        Response response = new Response(request);

        Assert.assertNull( resource.put( null, request, response, testRequest ) );
        Assert.assertEquals( 204, response.getStatus().getCode() );
    }

    public void testSuccessWithNoPass() throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
//        dto.setSystemPassword( "secret" );
//        dto.setSystemUsername( "uid=admin,ou=system" );
        dto.setProtocol( "ldap" );
        dto.setAuthScheme( "none" );
        dto.setRealm( "localhost" );

        Request request = new Request();
        Response response = new Response(request);

        Assert.assertNull( resource.put( null, request, response, testRequest ) );
        Assert.assertEquals( 204, response.getStatus().getCode() );
    }

    public void testFailure() throws Exception
    {

        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
//        dto.setSystemPassword( "secret" );
//        dto.setSystemUsername( "admin" );
        dto.setProtocol( "ldap" );
        dto.setAuthScheme( "DIGEST-MD5" );
        dto.setRealm( "localhost" );

        Request request = new Request();
        Response response = new Response(request);

        try
        {
            resource.put( null, request, response, testRequest );
            Assert.fail( "Expected ResourceException" );
        }
        catch( ResourceException e)
        {
            Assert.assertEquals( 400, e.getStatus().getCode() );
        }
    }

    public void testFailureWrongPass() throws Exception
    {
        PlexusResource resource = getResource();
        LdapAuthenticationTestRequest testRequest = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        testRequest.setData( dto );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setSystemPassword( "JUNK" );
        dto.setSystemUsername( "admin" );
        dto.setProtocol( "ldap" );
        dto.setAuthScheme( "DIGEST-MD5" );
        dto.setRealm( "localhost" );

        Request request = new Request();
        Response response = new Response(request);

        try
        {
            resource.put( null, request, response, testRequest );
            Assert.fail( "Expected ResourceException" );
        }
        catch( ResourceException e)
        {
            Assert.assertEquals( 400, e.getStatus().getCode() );
        }
    }

    @Override
    protected void copyDefaultLdapConfigToPlace()
    throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/md5-ldap.xml" ), new FileOutputStream(
            getNexusLdapConfiguration() ) );
    }

}
