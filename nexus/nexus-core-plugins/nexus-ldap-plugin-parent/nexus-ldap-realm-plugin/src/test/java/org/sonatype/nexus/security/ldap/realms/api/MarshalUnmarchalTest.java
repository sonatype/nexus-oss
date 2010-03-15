/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import java.text.SimpleDateFormat;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.security.ldap.realms.api.XStreamInitalizer;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserListResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserResponseDTO;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapAuthenticationTestRequest;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequest;
import org.sonatype.nexus.security.ldap.realms.test.api.dto.LdapUserAndGroupConfigTestRequestDTO;
import org.sonatype.plexus.rest.xstream.xml.LookAheadXppDriver;

import com.thoughtworks.xstream.XStream;

public class MarshalUnmarchalTest extends TestCase
{

    private SimpleDateFormat dateFormat = new SimpleDateFormat( "MM/dd/yyyy" );

    private XStream xstreamXML;

    private XStream xstreamJSON;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        xstreamXML = new XStream( new LookAheadXppDriver() );
        new XStreamInitalizer().initXStream( xstreamXML );

//        xstreamJSON = napp.doConfigureXstream( new XStream( new JsonOrgHierarchicalStreamDriver() ) );
//        new XStreamInitalizer().initXStream( xstreamJSON );
    }
    
    public void testLdapConnectionInfoResponse() throws Exception
    {
        LdapConnectionInfoResponse resource = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        
        resource.setData( dto );

        dto.setAuthScheme( "authScheme" );
        dto.setHost( "host" );
        dto.setPort( 123 );
        dto.setProtocol( "protocol" );
        dto.setRealm( "realm" );
        dto.setSearchBase( "searchBase" );
        dto.setSystemPassword( "systemPassword" );
        dto.setSystemUsername( "systemUsername" );
        
        validateXmlHasNoPackageNames( resource );    
    }
    
    
    
    public void testLdapUserAndGroupConfigurationResponse() throws Exception
    {
        LdapUserAndGroupConfigurationResponse resource = new LdapUserAndGroupConfigurationResponse();
        LdapUserAndGroupConfigurationDTO dto = new LdapUserAndGroupConfigurationDTO();
        
        resource.setData( dto );
        
        dto.setUserMemberOfAttribute( "userMemberOfAttribute" );
        dto.setEmailAddressAttribute( "emailAddressAttribute" );
        dto.setGroupBaseDn( "groupBaseDn" );
        dto.setGroupIdAttribute( "groupIdAttribute" );
        dto.setGroupMemberFormat( "groupMemberFormat" );
        dto.setGroupMemberAttribute( "groupMemberAttribute" );
        dto.setGroupMemberFormat( "groupMemberFormat" );
        dto.setGroupObjectClass( "groupObjectClass" );
        dto.setUserBaseDn( "userBaseDn" );
        dto.setUserIdAttribute( "userIdAttribute" );
        dto.setUserObjectClass( "userObjectClass" );
        dto.setUserPasswordAttribute( "userPasswordAttribute" );
        dto.setUserRealNameAttribute( "userRealNameAttribute" );
        dto.setUserSubtree( true );
        
        validateXmlHasNoPackageNames( resource );    
    }
    
    public void testLdapUserListResponse() throws Exception
    {
        LdapUserListResponse resource = new LdapUserListResponse();
        LdapUserResponseDTO dto1 = new LdapUserResponseDTO();
        resource.addLdapUserRoleMapping( dto1 );

        dto1.setEmail( "email1" );
        dto1.setName( "name1" );
        dto1.setUserId( "userId1" );
        dto1.addRole( "role1" );
        dto1.addRole( "role2" );
        
        LdapUserResponseDTO dto2 = new LdapUserResponseDTO();
        resource.addLdapUserRoleMapping( dto2 );
        
        dto2.setEmail( "email2" );
        dto2.setName( "name2" );
        dto2.setUserId( "userId2" );
        dto2.addRole( "role3" );
        dto2.addRole( "role4" );
        
        validateXmlHasNoPackageNames( resource );    
    }
  
    
    public void testLdapUserAndGroupConfigTestRequest() throws Exception
    {
        LdapUserAndGroupConfigTestRequest resource = new LdapUserAndGroupConfigTestRequest();
        LdapUserAndGroupConfigTestRequestDTO dto = new LdapUserAndGroupConfigTestRequestDTO();
        
        resource.setData( dto );

        dto.setAuthScheme( "authScheme" );
        dto.setHost( "host" );
        dto.setPort( 123 );
        dto.setProtocol( "protocol" );
        dto.setRealm( "realm" );
        dto.setSearchBase( "searchBase" );
        dto.setSystemPassword( "systemPassword" );
        dto.setSystemUsername( "systemUsername" );
        dto.setUserMemberOfAttribute( "userMemberOfAttribute" );
        dto.setEmailAddressAttribute( "emailAddressAttribute" );
        dto.setGroupBaseDn( "groupBaseDn" );
        dto.setGroupIdAttribute( "groupIdAttribute" );
        dto.setGroupMemberFormat( "groupMemberFormat" );
        dto.setGroupMemberAttribute( "groupMemberAttribute" );
        dto.setGroupMemberFormat( "groupMemberFormat" );
        dto.setGroupObjectClass( "groupObjectClass" );
        dto.setUserBaseDn( "userBaseDn" );
        dto.setUserIdAttribute( "userIdAttribute" );
        dto.setUserObjectClass( "userObjectClass" );
        dto.setUserPasswordAttribute( "userPasswordAttribute" );
        dto.setUserRealNameAttribute( "userRealNameAttribute" );
        dto.setUserSubtree( true );
        
        validateXmlHasNoPackageNames( resource );    
    }
    
    public void testLdapAuthenticationTestRequest() throws Exception
    {
        LdapAuthenticationTestRequest resource = new LdapAuthenticationTestRequest();
        LdapConnectionInfoDTO dto = new LdapConnectionInfoDTO();
        
        resource.setData( dto );

        dto.setAuthScheme( "authScheme" );
        dto.setHost( "host" );
        dto.setPort( 123 );
        dto.setProtocol( "protocol" );
        dto.setRealm( "realm" );
        dto.setSearchBase( "searchBase" );
        dto.setSystemPassword( "systemPassword" );
        dto.setSystemUsername( "systemUsername" );
        
        validateXmlHasNoPackageNames( resource );    
    }
    
    
    private void validateXmlHasNoPackageNames( Object obj )
    {
        String xml = this.xstreamXML.toXML( obj );

        // quick way of looking for the class="org attribute
        // i don't want to parse a dom to figure this out

        int totalCount = StringUtils.countMatches( xml, "org.sonatype" );
        totalCount += StringUtils.countMatches( xml, "com.sonatype" );

        // check the counts
        Assert.assertFalse( "Found package name in XML:\n" + xml, totalCount > 0 );

        // // print out each type of method, so i can rafb it
        // System.out.println( "\n\nClass: "+ obj.getClass() +"\n" );
        // System.out.println( xml+"\n" );
        //        
        // Assert.assertFalse( "Found <string> XML: " + obj.getClass() + "\n" + xml, xml.contains( "<string>" ) );

    }
    
    
}
