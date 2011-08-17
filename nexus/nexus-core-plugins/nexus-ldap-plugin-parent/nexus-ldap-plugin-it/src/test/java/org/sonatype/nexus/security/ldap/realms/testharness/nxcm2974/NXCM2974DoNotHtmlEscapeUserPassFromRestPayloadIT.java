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
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm2974;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.sonatype.nexus.test.utils.ResponseMatchers.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequestDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapConnMessageUtil;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUserGroupMessageUtil;
import org.testng.annotations.Test;

public class NXCM2974DoNotHtmlEscapeUserPassFromRestPayloadIT
    extends AbstractLdapIntegrationIT
{

    @Test
    public void testSave()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        LdapConnMessageUtil connUtil = new LdapConnMessageUtil( getJsonXStream(), MediaType.APPLICATION_JSON );
        LdapConnectionInfoDTO request = getConnectionInfo();
        LdapConnectionInfoDTO responseConnInfo = connUtil.updateConnectionInfo( request );
        responseConnInfo.setSystemPassword( "" );
        request.setSystemPassword( "" );

        assertThat( responseConnInfo, equalTo( request ) );

        // test for correct ldap config
        File ldapConfigFile = new File( WORK_CONF_DIR, "/ldap.xml" );
        assertThat( "cannot find ldap config", ldapConfigFile.exists() );
        assertThat( FileUtils.readFileToString( ldapConfigFile ), containsString( "more&amp;more" ) );

        connUtil.validateLdapConfig( getConnectionInfo() );

        Response response = connUtil.sendTestMessage( getConnectionInfo() );
        try
        {
            assertThat( response, isSuccessful() );
        }
        finally
        {
            RequestFacade.releaseResponse( response );
        }
    }

    @Test
    public void testLdapUsersTest()
        throws Exception
    {
        LdapConnMessageUtil connUtil = new LdapConnMessageUtil( getJsonXStream(), MediaType.APPLICATION_JSON );
        LdapUserGroupMessageUtil userUtil = new LdapUserGroupMessageUtil( getJsonXStream(), MediaType.APPLICATION_JSON );

        connUtil.updateConnectionInfo( getConnectionInfo() );
        LdapUserAndGroupConfigurationDTO userGroupConfig = userUtil.updateUserGroupConfig( getUserGroupConf() );

        assertThat( userGroupConfig, equalTo( getUserGroupConf() ) );

        userUtil.validateLdapConfig( getUserGroupConf() );
        Response response = userUtil.sendTestMessage( getTestUserGroupRequest() );
        try
        {
            assertThat( response, isSuccessful() );
        }
        finally
        {
            RequestFacade.releaseResponse( response );
        }

    }

    private LdapUserAndGroupConfigTestRequestDTO getTestUserGroupRequest()
    {
        LdapUserAndGroupConfigTestRequestDTO requestDTO = new LdapUserAndGroupConfigTestRequestDTO();
        requestDTO.setUserMemberOfAttribute( "businesscategory" );
        requestDTO.setGroupBaseDn( "ou=groups" );
        requestDTO.setGroupIdAttribute( "cn" );
        requestDTO.setGroupMemberAttribute( "groupMemberAttribute" );
        requestDTO.setGroupMemberFormat( "cn=${username},ou=groups,o=more&more" );
        requestDTO.setGroupObjectClass( "organizationalRole" );
        requestDTO.setLdapGroupsAsRoles( true );

        requestDTO.setEmailAddressAttribute( "mail" );
        requestDTO.setUserBaseDn( "ou=people" );
        requestDTO.setUserIdAttribute( "uid" );
        requestDTO.setUserObjectClass( "inetOrgPerson" );
        requestDTO.setUserPasswordAttribute( "userPassword" );
        requestDTO.setUserRealNameAttribute( "sn" );
        requestDTO.setUserSubtree( true );

        requestDTO.setAuthScheme( "simple" );
        requestDTO.setHost( "localhost" );
        requestDTO.setPort( this.getLdapPort() );
        requestDTO.setProtocol( "ldap" );
        // connInfo.setRealm( "" );
        requestDTO.setSearchBase( "o=more&more" );
        requestDTO.setSystemPassword( "secret" );
        requestDTO.setSystemUsername( "uid=admin,ou=system" );

        return requestDTO;

    }

    private LdapUserAndGroupConfigurationDTO getUserGroupConf()
    {
        LdapUserAndGroupConfigurationDTO userGroupConf = new LdapUserAndGroupConfigurationDTO();
        userGroupConf.setUserMemberOfAttribute( "businesscategory" );
        userGroupConf.setGroupBaseDn( "ou=groups" );
        userGroupConf.setGroupIdAttribute( "cn" );
        userGroupConf.setGroupMemberAttribute( "groupMemberAttribute" );
        userGroupConf.setGroupMemberFormat( "cn=${username},ou=groups,o=more&more" );
        userGroupConf.setGroupObjectClass( "organizationalRole" );
        userGroupConf.setLdapGroupsAsRoles( true );

        userGroupConf.setEmailAddressAttribute( "mail" );
        userGroupConf.setUserBaseDn( "ou=people" );
        userGroupConf.setUserIdAttribute( "uid" );
        userGroupConf.setUserObjectClass( "inetOrgPerson" );
        userGroupConf.setUserPasswordAttribute( "userPassword" );
        userGroupConf.setUserRealNameAttribute( "sn" );
        userGroupConf.setUserSubtree( true );
        return userGroupConf;
    }

    private LdapConnectionInfoDTO getConnectionInfo()
    {
        LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();

        connInfo.setAuthScheme( "simple" );
        connInfo.setHost( "localhost" );
        connInfo.setPort( this.getLdapPort() );
        connInfo.setProtocol( "ldap" );
        // connInfo.setRealm( "" );
        connInfo.setSearchBase( "o=more&more" );
        connInfo.setSystemPassword( "secret" );
        connInfo.setSystemUsername( "uid=admin,ou=system" );
        return connInfo;
    }
}
