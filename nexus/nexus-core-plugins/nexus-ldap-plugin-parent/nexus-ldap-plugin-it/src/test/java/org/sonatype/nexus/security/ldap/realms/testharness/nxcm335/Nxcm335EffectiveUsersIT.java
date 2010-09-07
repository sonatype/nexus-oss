/**
 * Sonatype NexusTM Professional.
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 */
package org.sonatype.nexus.security.ldap.realms.testharness.nxcm335;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUsersMessageUtil;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;
import org.sonatype.security.rest.model.UserToRoleResource;

import com.thoughtworks.xstream.XStream;

public class Nxcm335EffectiveUsersIT
    extends AbstractLdapIntegrationIT
{
    private XStream xstream;

    private MediaType mediaType;

    public Nxcm335EffectiveUsersIT()
    {
        super();
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
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 2 + defaultUserCount, users.size() );
        
        users = this.doSearch( "", true, "LDAP" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 2, users.size() );

        // map user to nexus role
        LdapUsersMessageUtil userUtil = new LdapUsersMessageUtil( this, this.xstream, this.mediaType );
        UserToRoleResource ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "cstamas" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        Response response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText(),
            response.getStatus().isSuccess() );

        // search effective users should find user
        users = this.doSearch( "", true, "all" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 2 + defaultUserCount, users.size() );
        
        users = this.doSearch( "", true, "LDAP" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 2, users.size() );

        ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "brianf" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText(),
            response.getStatus().isSuccess() );

        // search effective users should find user
        users = this.doSearch( "", true, "LDAP" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 2, users.size() );

        users = this.doSearch( "", true, "all" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 3 + defaultUserCount, users.size() );

    }

    @Test
    public void searchTestWithEffectiveUsersFalse()
        throws Exception
    {
        int defaultUserCount = this.doSearch( "", false, "default" ).size();
        defaultUserCount += this.doSearch( "", false, "Simple" ).size(); // the OSS ITs have a memory realm too

        // by default we should have 2 effective users ( using the developer role )
        List<PlexusUserResource> users = this.doSearch( "", false, "LDAP" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 4, users.size() );
        
        users = this.doSearch( "", false, "all" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), defaultUserCount + 4, users.size() );

        // map user to nexus role
        LdapUsersMessageUtil userUtil = new LdapUsersMessageUtil( this, this.xstream, this.mediaType );
        UserToRoleResource ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "cstamas" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        Response response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText(),
            response.getStatus().isSuccess() );

        // search effective users should find user
        users = this.doSearch( "", false, "LDAP" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 4, users.size() );
        
        users = this.doSearch( "", false, "all" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), defaultUserCount + 4, users.size() );

        ldapUser = new UserToRoleResource();
        ldapUser.setUserId( "brianf" );
        ldapUser.setSource( "LDAP" );
        ldapUser.addRole( "admin" );
        response = userUtil.sendMessage( Method.PUT, ldapUser, "LDAP" );
        Assert.assertTrue(
            "Status: " + response.getStatus() + "\nresponse: " + response.getEntity().getText(),
            response.getStatus().isSuccess() );

        // search effective users should find user
        users = this.doSearch( "", false, "LDAP" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), 4, users.size() );
        
        users = this.doSearch( "", false, "all" );
        Assert.assertEquals( "Users found: " + this.toUserIds( users ), defaultUserCount + 4, users.size() );

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

        Assert.assertTrue( "Status: " + response.getStatus(), response.getStatus().isSuccess() );

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
