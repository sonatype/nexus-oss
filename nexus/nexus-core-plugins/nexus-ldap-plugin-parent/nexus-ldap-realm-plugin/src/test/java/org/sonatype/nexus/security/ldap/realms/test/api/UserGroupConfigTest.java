/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.test.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserListResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserResponseDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequest;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequestDTO;
import org.sonatype.plexus.rest.resource.PlexusResource;


public class UserGroupConfigTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapUserAndGroupConfigTestPlexusResource" );
    }

    public void testSuccess()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );
        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "ou=people" );
        dto.setUserIdAttribute( "uid" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );

        Request request = new Request();
        Response response = new Response( request );

        LdapUserListResponse usersListResponse =
            (LdapUserListResponse) resource.put( null, request, response, testRequest );

        Assert.assertNotNull( usersListResponse );
        Assert.assertEquals( 200, response.getStatus().getCode() );

        List<LdapUserResponseDTO> users = usersListResponse.getLdapUserRoleMappings();

        Assert.assertEquals( 4, usersListResponse.getLdapUserRoleMappings().size() );

        // build a nice little map so we can test things without a else if
        Map<String, LdapUserResponseDTO> userMap = new HashMap<String, LdapUserResponseDTO>();
        for ( LdapUserResponseDTO user : usersListResponse.getLdapUserRoleMappings() )
        {
            userMap.put( user.getUserId(), user );
        }

        // now check everybody
        LdapUserResponseDTO cstamas = userMap.get( "cstamas" );
        Assert.assertEquals( "Tamas Cservenak", cstamas.getName() );
        Assert.assertEquals( "cstamas@sonatype.com", cstamas.getEmail() );
        Assert.assertEquals( 0, cstamas.getRoles().size() );

        LdapUserResponseDTO brianf = userMap.get( "brianf" );
        Assert.assertEquals( "Brian Fox", brianf.getName() );
        Assert.assertEquals( "brianf@sonatype.com", brianf.getEmail() );
        Assert.assertEquals( 0, brianf.getRoles().size() );

        LdapUserResponseDTO jvanzyl = userMap.get( "jvanzyl" );
        Assert.assertEquals( "Jason Van Zyl", jvanzyl.getName() );
        Assert.assertEquals( "jvanzyl@sonatype.com", jvanzyl.getEmail() );
        Assert.assertEquals( 0, jvanzyl.getRoles().size() );

        LdapUserResponseDTO jdcasey = userMap.get( "jdcasey" );
        Assert.assertEquals( "John Casey", jdcasey.getName() );
        Assert.assertEquals( "jdcasey@sonatype.com", jdcasey.getEmail() );
        Assert.assertEquals( 0, jdcasey.getRoles().size() );
    }

    public void testSuccessWithLimit()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        // limit to 3
        dto.setUserLimitCount( 3 );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );
        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "ou=people" );
        dto.setUserIdAttribute( "uid" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );

        Request request = new Request();
        Response response = new Response( request );

        LdapUserListResponse usersListResponse =
            (LdapUserListResponse) resource.put( null, request, response, testRequest );

        Assert.assertNotNull( usersListResponse );
        Assert.assertEquals( 200, response.getStatus().getCode() );

        Assert.assertEquals( 3, usersListResponse.getLdapUserRoleMappings().size() );
    }

    public void testSuccessUsingLdapGroups()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );
        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "ou=people" );
        dto.setUserIdAttribute( "uid" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );
        dto.setLdapGroupsAsRoles( true );

        Request request = new Request();
        Response response = new Response( request );

        LdapUserListResponse usersListResponse =
            (LdapUserListResponse) resource.put( null, request, response, testRequest );

        Assert.assertNotNull( usersListResponse );
        Assert.assertEquals( 200, response.getStatus().getCode() );

        List<LdapUserResponseDTO> users = usersListResponse.getLdapUserRoleMappings();

        Assert.assertEquals( 4, usersListResponse.getLdapUserRoleMappings().size() );

        // build a nice little map so we can test things without a else if
        Map<String, LdapUserResponseDTO> userMap = new HashMap<String, LdapUserResponseDTO>();
        for ( LdapUserResponseDTO user : usersListResponse.getLdapUserRoleMappings() )
        {
            userMap.put( user.getUserId(), user );
        }

        // now check everybody
        LdapUserResponseDTO cstamas = userMap.get( "cstamas" );
        Assert.assertEquals( "Tamas Cservenak", cstamas.getName() );
        Assert.assertEquals( "cstamas@sonatype.com", cstamas.getEmail() );
        Assert.assertEquals( 2, cstamas.getRoles().size() );
        Assert.assertTrue( cstamas.getRoles().contains( "repoconsumer" ) );
        Assert.assertTrue( cstamas.getRoles().contains( "developer" ) );

        LdapUserResponseDTO brianf = userMap.get( "brianf" );
        Assert.assertEquals( "Brian Fox", brianf.getName() );
        Assert.assertEquals( "brianf@sonatype.com", brianf.getEmail() );
        Assert.assertEquals( 2, brianf.getRoles().size() );
        Assert.assertTrue( brianf.getRoles().contains( "repoconsumer" ) );
        Assert.assertTrue( brianf.getRoles().contains( "repomaintainer" ) );

        LdapUserResponseDTO jvanzyl = userMap.get( "jvanzyl" );
        Assert.assertEquals( "Jason Van Zyl", jvanzyl.getName() );
        Assert.assertEquals( "jvanzyl@sonatype.com", jvanzyl.getEmail() );
        Assert.assertEquals( 3, jvanzyl.getRoles().size() );
        Assert.assertTrue( jvanzyl.getRoles().contains( "repoconsumer" ) );
        Assert.assertTrue( jvanzyl.getRoles().contains( "repomaintainer" ) );
        Assert.assertTrue( jvanzyl.getRoles().contains( "developer" ) );

        LdapUserResponseDTO jdcasey = userMap.get( "jdcasey" );
        Assert.assertEquals( "John Casey", jdcasey.getName() );
        Assert.assertEquals( "jdcasey@sonatype.com", jdcasey.getEmail() );
        Assert.assertEquals( 0, jdcasey.getRoles().size() );
    }

    public void testWithValidationFailure()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        // dto.setProtocol( "ldap" );
        // dto.setHost( "localhost" );
        // dto.setPort( 12345 );
        // dto.setSearchBase( "o=sonatype" );
        // dto.setAuthScheme( "none" );

        // dto.setGroupMemberFormat("uid=${username},ou=people,o=sonatype");
        // dto.setGroupObjectClass("groupOfUniqueNames");
        // dto.setGroupBaseDn("ou=groups");
        // dto.setGroupIdAttribute("cn");
        // dto.setGroupMemberAttribute("uniqueMember");
        // dto.setUserObjectClass("inetOrgPerson");
        // dto.setUserBaseDn( "ou=people" );
        // dto.setUserIdAttribute("uid");
        // dto.setUserPasswordAttribute("userPassword");
        // dto.setUserRealNameAttribute("sn");
        // dto.setEmailAddressAttribute( "mail");
        // dto.setPreferredPasswordEncoding("clear");

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

    public void testFailBadUserDN()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );

        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "ou=JUNK" );
        dto.setUserIdAttribute( "uid" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );

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

    public void testFailInvalidUserDN()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );

        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "JUNK" );
        dto.setUserIdAttribute( "uid" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );
        dto.setLdapGroupsAsRoles( true );

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

    public void testInvalidConfigWithResults()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "Foo" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );

        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "ou=people" );
        dto.setUserIdAttribute( "mail" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );
        dto.setLdapGroupsAsRoles( true );

        Request request = new Request();
        Response response = new Response( request );

        LdapUserListResponse usersListResponse =
            (LdapUserListResponse) resource.put( null, request, response, testRequest );

        Assert.assertNotNull( usersListResponse );
        Assert.assertEquals( 200, response.getStatus().getCode() );

        Assert.assertEquals( 4, usersListResponse.getLdapUserRoleMappings().size() );

        // none of the users should have any roles
        for ( LdapUserResponseDTO user : usersListResponse.getLdapUserRoleMappings() )
        {
            Assert.assertTrue( "Expected user to have 0 roles.", user.getRoles().isEmpty() );
        }
    }

    public void testBadConnInfoFailure()
        throws Exception
    {
        PlexusResource resource = getResource();
        LdapUserAndGroupConfigTestRequest testRequest = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        testRequest.setData( dto );

        dto.setProtocol( "ldap" );
        dto.setHost( "localhost" );
        dto.setPort( 123456 );
        dto.setSearchBase( "o=sonatype" );
        dto.setAuthScheme( "none" );

        dto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        dto.setGroupObjectClass( "groupOfUniqueNames" );
        dto.setGroupBaseDn( "ou=groups" );
        dto.setGroupIdAttribute( "cn" );
        dto.setGroupMemberAttribute( "uniqueMember" );
        dto.setUserObjectClass( "inetOrgPerson" );
        dto.setUserBaseDn( "ou=people" );
        dto.setUserIdAttribute( "uid" );
        dto.setUserPasswordAttribute( "userPassword" );
        dto.setUserRealNameAttribute( "sn" );
        dto.setEmailAddressAttribute( "mail" );
        dto.setPreferredPasswordEncoding( "clear" );

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
