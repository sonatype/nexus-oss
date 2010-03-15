/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm58;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequestDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapConnMessageUtil;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUserGroupMessageUtil;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUsersMessageUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.security.rest.model.UserToRoleResource;

import com.thoughtworks.xstream.XStream;

public class Nxcm58NexusCommonUseJsonIT
    extends AbstractLdapIntegrationIT
{

    protected XStream xstream;

    protected MediaType mediaType;

    public Nxcm58NexusCommonUseJsonIT()
    {
        super();
        this.xstream = this.getJsonXStream();
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        // copy ldap.xml to work dir
        // this.copyConfigFile( "ldap.xml", RELATIVE_WORK_CONF_DIR );

        // delete the ldap file.
        Assert.assertTrue( "Failed to delete the ldap.xml config file", this.deleteLdapConfig() );

        this.copyConfigFile( "test.ldif", LDIF_DIR );
    }

    @Test
    public void testNormalUsePattern()
        throws Exception
    {
        LdapConnMessageUtil connUtil = new LdapConnMessageUtil( this.xstream, this.mediaType );
        LdapUserGroupMessageUtil userGroupUtil = new LdapUserGroupMessageUtil( this.xstream, this.mediaType );
        LdapUsersMessageUtil userUtil = new LdapUsersMessageUtil( this.xstream, this.mediaType );

        // get
        LdapConnectionInfoDTO dto = connUtil.getConnectionInfo();
        Assert.assertEquals( new LdapConnectionInfoDTO(), dto ); // expected empty object

        // configure LDAP connection info
        dto = new LdapConnectionInfoDTO();
        dto.setAuthScheme( "none" );
        dto.setHost( "localhost" );
        dto.setPort( 12345 );
        dto.setProtocol( "ldap" );
        dto.setSearchBase( "o=sonatype" );

        // test
        Response testResponse = connUtil.sendTestMessage( dto );
        Assert.assertEquals( Status.SUCCESS_NO_CONTENT.getCode(), testResponse.getStatus().getCode() );

        // save
        connUtil.updateConnectionInfo( dto );

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

        // test
        testResponse = userGroupUtil.sendTestMessage( userGroupTestDto );
        Assert.assertEquals( "Test connection failed: " + testResponse.getEntity().getText(), Status.SUCCESS_OK
            .getCode(), testResponse.getStatus().getCode() );
        // TODO: check result

        LdapUserAndGroupConfigurationDTO userGroupConfig = new LdapUserAndGroupConfigurationDTO();
        userGroupConfig.setGroupMemberFormat( userGroupTestDto.getGroupMemberFormat() );
        userGroupConfig.setGroupObjectClass( userGroupTestDto.getGroupObjectClass() );
        userGroupConfig.setGroupBaseDn( userGroupTestDto.getGroupBaseDn() );
        userGroupConfig.setGroupIdAttribute( userGroupTestDto.getGroupIdAttribute() );
        userGroupConfig.setGroupMemberAttribute( userGroupTestDto.getGroupMemberAttribute() );
        userGroupConfig.setUserObjectClass( userGroupTestDto.getUserObjectClass() );
        userGroupConfig.setUserBaseDn( userGroupTestDto.getUserBaseDn() );
        userGroupConfig.setUserIdAttribute( userGroupTestDto.getUserIdAttribute() );
        userGroupConfig.setUserPasswordAttribute( userGroupTestDto.getUserPasswordAttribute() );
        userGroupConfig.setUserRealNameAttribute( userGroupTestDto.getUserRealNameAttribute() );
        userGroupConfig.setEmailAddressAttribute( userGroupTestDto.getEmailAddressAttribute() );
        userGroupConfig.setLdapGroupsAsRoles( userGroupTestDto.isLdapGroupsAsRoles() );
        userGroupConfig.setUserSubtree( userGroupTestDto.isUserSubtree() );
        userGroupConfig.setGroupSubtree( userGroupTestDto.isGroupSubtree() );
        userGroupConfig.setUserMemberOfAttribute( userGroupTestDto.getUserMemberOfAttribute() );
        // save
        userGroupUtil.updateUserGroupConfig( userGroupConfig );

        // enable ldap realms
        GlobalConfigurationResource globalConfig = SettingsMessageUtil.getCurrentSettings();
        globalConfig.addSecurityRealm( "NexusLdapAuthenticationRealm" );
        Status status = SettingsMessageUtil.save( globalConfig );
        Assert.assertTrue( status.isSuccess() );

     // configure users
        UserToRoleResource ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "cstamas" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        Response response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText(),
            response.getStatus().isSuccess() );

        // login/do something with an ldap user
        TestContainer.getInstance().getTestContext().setUsername( "cstamas" );
        TestContainer.getInstance().getTestContext().setPassword( "cstamas123" );
        response = RequestFacade.sendMessage( "service/local/global_settings/current", Method.GET );
        Assert.assertTrue(
            "Status: " + response.getStatus() + "\nresponse Text: " + response.getEntity().getText(),
            response.getStatus().isSuccess() );

        // wrong password
        TestContainer.getInstance().getTestContext().setPassword( "WRONG-PASSWORD" );
        response = RequestFacade.sendMessage( "service/local/global_settings/current", Method.GET );
        Assert.assertFalse( response.getStatus().isSuccess() );

        // non existing user
        TestContainer.getInstance().getTestContext().setUsername( "JUNK" );
        TestContainer.getInstance().getTestContext().setPassword( "password" );
        response = RequestFacade.sendMessage( "service/local/global_settings/current", Method.GET );
        Assert.assertFalse( response.getStatus().isSuccess() );

        // now make sure the xml admin still works
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        response = RequestFacade.sendMessage( "service/local/global_settings/current", Method.GET );
        Assert.assertTrue( response.getStatus().isSuccess() );
    }

}
