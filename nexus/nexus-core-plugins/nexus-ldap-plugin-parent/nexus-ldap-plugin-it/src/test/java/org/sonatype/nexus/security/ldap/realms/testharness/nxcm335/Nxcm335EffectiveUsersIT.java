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
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm335;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUsersMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.sonatype.security.rest.model.UserToRoleResource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

public class Nxcm335EffectiveUsersIT
    extends AbstractLdapIntegrationIT
{
    private XStream xstream;

    private MediaType mediaType;

    public Nxcm335EffectiveUsersIT()
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
    public void searchTestWithEffectiveUsers()
        throws Exception
    {   
        int defaultUserCount = this.doSearch( "", false, "default" ).size();

        // by default we should have 2 effective users ( using the developer role )
        List<PlexusUserResource> users = this.doSearch( "", true, "all" );
        Assert.assertEquals( users.size(), 2 + defaultUserCount, "Users found: " + this.toUserIds( users ) );
        
        users = this.doSearch( "", true, "LDAP" );
        Assert.assertEquals( users.size(), 2, "Users found: " + this.toUserIds( users ) );

        // map user to nexus role
        LdapUsersMessageUtil userUtil = new LdapUsersMessageUtil( this, this.xstream, this.mediaType );
        UserToRoleResource ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "cstamas" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        Response response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            response.getStatus().isSuccess(),
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText() );

        // search effective users should find user
        users = this.doSearch( "", true, "all" );
        Assert.assertEquals( users.size(), 2 + defaultUserCount, "Users found: " + this.toUserIds( users ) );
        
        users = this.doSearch( "", true, "LDAP" );
        Assert.assertEquals( users.size(), 2, "Users found: " + this.toUserIds( users ) );

        ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "brianf" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            response.getStatus().isSuccess(),
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText() );

        // search effective users should find user
        users = this.doSearch( "", true, "LDAP" );
        Assert.assertEquals( users.size(), 2, "Users found: " + this.toUserIds( users ) );

        users = this.doSearch( "", true, "all" );
        Assert.assertEquals( users.size(), 3 + defaultUserCount, "Users found: " + this.toUserIds( users ) );

    }

    @Test
    public void searchTestWithEffectiveUsersFalse()
        throws Exception
    {
        int defaultUserCount = this.doSearch( "", false, "default" ).size();
        defaultUserCount += this.doSearch( "", false, "Simple" ).size(); // the OSS ITs have a memory realm too
        
        // the xmlrealm tests bring in a couple too ( now that classpath scanning is enabled )
        defaultUserCount += this.doSearch( "", false, "MockUserManagerA" ).size();
        defaultUserCount += this.doSearch( "", false, "MockUserManagerB" ).size();

        // by default we should have 2 effective users ( using the developer role )
        List<PlexusUserResource> users = this.doSearch( "", false, "LDAP" );
        Assert.assertEquals( users.size(), 4, "Users found: " + this.toUserIds( users ) );
        
        users = this.doSearch( "", false, "all" );
        Assert.assertEquals( users.size(), defaultUserCount + 4, "Users found: " + this.toUserIds( users ) );

        // map user to nexus role
        LdapUsersMessageUtil userUtil = new LdapUsersMessageUtil( this, this.xstream, this.mediaType );
        UserToRoleResource ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "cstamas" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        Response response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            response.getStatus().isSuccess(),
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText() );

        // search effective users should find user
        users = this.doSearch( "", false, "LDAP" );
        Assert.assertEquals( users.size(), 4, "Users found: " + this.toUserIds( users ) );
        
        users = this.doSearch( "", false, "all" );
        Assert.assertEquals( users.size(), defaultUserCount + 4, "Users found: " + this.toUserIds( users ) );

        ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "brianf" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            response.getStatus().isSuccess(),
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText() );

        // search effective users should find user
        users = this.doSearch( "", false, "LDAP" );
        Assert.assertEquals( users.size(), 4, "Users found: " + this.toUserIds( users ) );
        
        users = this.doSearch( "", false, "all" );
        Assert.assertEquals( users.size(), defaultUserCount + 4, "Users found: " + this.toUserIds( users ) );

    }

    @SuppressWarnings( "unchecked" )
    private List<PlexusUserResource> doSearch( String userId, boolean effective, String source )
        throws IOException
    {
        PlexusUserSearchCriteriaResourceRequest resourceRequest = new PlexusUserSearchCriteriaResourceRequest();
        PlexusUserSearchCriteriaResource criteria = new PlexusUserSearchCriteriaResource();
        criteria.setUserId( userId );
        criteria.setEffectiveUsers( effective );
        resourceRequest.setData( criteria );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = RequestFacade.SERVICE_LOCAL + "user_search/" + source;

        // now set the payload
        representation.setPayload( resourceRequest );

        log.debug( "sendMessage: " + representation.getText() );

        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );

        Assert.assertTrue( response.getStatus().isSuccess(), "Status: " + response.getStatus() );

        PlexusUserListResourceResponse userList = (PlexusUserListResourceResponse) this.parseResponse(
            response,
            new PlexusUserListResourceResponse() );

        return userList.getData();
    }

    private Object parseResponse( Response response, Object expectedObject )
        throws IOException
    {

        String responseString = response.getEntity().getText();
        log.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        return representation.getPayload( expectedObject );
    }

    private List<String> toUserIds( List<PlexusUserResource> users )
    {
        List<String> ids = new ArrayList<String>();

        for ( PlexusUserResource user : users )
        {
            ids.add( user.getUserId() );
        }
        return ids;
    }

}
