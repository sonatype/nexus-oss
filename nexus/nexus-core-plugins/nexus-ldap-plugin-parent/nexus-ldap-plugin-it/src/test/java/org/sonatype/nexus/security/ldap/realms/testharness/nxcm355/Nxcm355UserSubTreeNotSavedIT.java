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
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm355;

import org.restlet.data.MediaType;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUserGroupMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

public class Nxcm355UserSubTreeNotSavedIT
    extends AbstractLdapIntegrationIT
{
    private XStream xstream;

    private MediaType mediaType;

    public Nxcm355UserSubTreeNotSavedIT()
    {
        super();
    }

    @BeforeClass
    public void init()
    {
        this.xstream = this.getJsonXStream();
        this.mediaType = MediaType.APPLICATION_JSON;
    }

    @Test
    public void saveUserAndGroupConfigWithUserSubtree()
        throws Exception
    {
        LdapUserGroupMessageUtil userGroupUtil = new LdapUserGroupMessageUtil( this.xstream, this.mediaType );

        // configure LDAP user/group config
        LdapUserAndGroupConfigurationDTO userGroupDto = new LdapUserAndGroupConfigurationDTO();

        userGroupDto.setGroupMemberFormat( "uid=${username},ou=people,o=sonatype" );
        userGroupDto.setGroupObjectClass( "groupOfUniqueNames" );
        userGroupDto.setGroupBaseDn( "ou=groups" );
        userGroupDto.setGroupIdAttribute( "cn" );
        userGroupDto.setGroupMemberAttribute( "uniqueMember" );
        userGroupDto.setUserObjectClass( "inetOrgPerson" );
        userGroupDto.setUserBaseDn( "ou=people" );
        userGroupDto.setUserIdAttribute( "uid" );
        userGroupDto.setUserPasswordAttribute( "userpassword" );
        userGroupDto.setUserRealNameAttribute( "sn" );
        userGroupDto.setEmailAddressAttribute( "mail" );
        userGroupDto.setLdapGroupsAsRoles( false );
        // the problem was that subtree was getting set to groupSubtree
        userGroupDto.setUserSubtree( true );
        userGroupDto.setGroupSubtree( false );
        userGroupDto.setUserMemberOfAttribute( "" );

        LdapUserAndGroupConfigurationDTO result = userGroupUtil.updateUserGroupConfig( userGroupDto );
        Assert.assertEquals( userGroupDto, result );

    }
}
