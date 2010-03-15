/**
 * Sonatype NexusTM Professional.
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 */
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm355;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUserGroupMessageUtil;

import com.thoughtworks.xstream.XStream;

public class Nxcm355UserSubTreeNotSavedIT extends AbstractLdapIntegrationIT
{
    private XStream xstream;
    private MediaType mediaType;
    
    public Nxcm355UserSubTreeNotSavedIT()
    {   
        super();
        this.xstream = this.getJsonXStream();
        this.mediaType = MediaType.APPLICATION_JSON;
    }
    
    @Test
    public void saveUserAndGroupConfigWithUserSubtree() throws Exception
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
