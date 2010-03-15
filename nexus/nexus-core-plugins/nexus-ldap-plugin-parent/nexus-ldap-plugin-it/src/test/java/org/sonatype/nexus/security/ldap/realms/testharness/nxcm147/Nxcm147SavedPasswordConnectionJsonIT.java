/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm147;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.security.ldap.realms.api.LdapRealmPlexusResourceConst;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequestDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapConnMessageUtil;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUserGroupMessageUtil;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUsersMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nxcm147SavedPasswordConnectionJsonIT extends AbstractLdapIntegrationIT
{
    private XStream xstream;
    private MediaType mediaType;

    public Nxcm147SavedPasswordConnectionJsonIT()
    {
        super();
        this.xstream = this.getJsonXStream();
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    @Test
    public void connectionTestWithFakePassword() throws Exception
    {

        LdapConnMessageUtil connUtil = new LdapConnMessageUtil( this.xstream, this.mediaType );
        LdapUserGroupMessageUtil userGroupUtil = new LdapUserGroupMessageUtil( this.xstream, this.mediaType );
        LdapUsersMessageUtil userUtil = new LdapUsersMessageUtil( this.xstream, this.mediaType );

        // get
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        dto.setAuthScheme( "simple" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );
        dto.setSystemUsername( "uid=admin,ou=system" );
        dto.setSystemPassword( "secret" );
        dto = connUtil.updateConnectionInfo( dto );
        Assert.assertEquals( LdapRealmPlexusResourceConst.FAKE_PASSWORD, dto.getSystemPassword() );

        // test
        Response testResponse = connUtil.sendTestMessage( dto );
        Assert.assertEquals( Status.SUCCESS_NO_CONTENT.getCode(), testResponse.getStatus().getCode() );

     // configure LDAP user/group config
        LdapUserAndGroupConfigTestRequestDTO userGroupTestDto = new LdapUserAndGroupConfigTestRequestDTO();
        userGroupTestDto.setAuthScheme( dto.getAuthScheme() );
        userGroupTestDto.setHost( dto.getHost() );
        userGroupTestDto.setPort( dto.getPort() );
        userGroupTestDto.setProtocol( dto.getProtocol() );
        userGroupTestDto.setSearchBase( dto.getSearchBase() );
        userGroupTestDto.setSystemUsername( dto.getSystemUsername() );
        userGroupTestDto.setSystemPassword( dto.getSystemPassword() );

        userGroupTestDto.setUserMemberOfAttribute( "" );

        userGroupTestDto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        userGroupTestDto.setGroupObjectClass( "groupOfUniqueNames" );
        userGroupTestDto.setGroupBaseDn( "ou=groups" );
        userGroupTestDto.setGroupIdAttribute( "cn" );
        userGroupTestDto.setGroupMemberAttribute( "uniqueMember" );
        userGroupTestDto.setUserObjectClass( "inetOrgPerson" );
        userGroupTestDto.setUserBaseDn( "ou=people" );
        userGroupTestDto.setUserIdAttribute( "uid" );
        userGroupTestDto.setUserPasswordAttribute( "userpassword" );
        userGroupTestDto.setUserRealNameAttribute( "sn" );
        userGroupTestDto.setEmailAddressAttribute( "mail" );
        userGroupTestDto.setLdapGroupsAsRoles( false );
        userGroupTestDto.setUserSubtree( false );
        userGroupTestDto.setGroupSubtree( false );
        userGroupTestDto.setUserMemberOfAttribute( "" );

        testResponse = userGroupUtil.sendTestMessage( userGroupTestDto );
        Assert.assertEquals( Status.SUCCESS_OK.getCode(), testResponse.getStatus().getCode() );



    }
}
