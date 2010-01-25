package org.sonatype.nexus.security.ldap.realms.testharness.nexus3244;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserAndGroupConfigurationDTO;
import org.sonatype.nexus.security.ldap.realms.testharness.AbstractLdapIntegrationIT;
import org.sonatype.nexus.security.ldap.realms.testharness.LdapUserGroupMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;

import com.thoughtworks.xstream.XStream;

public class Nexus3244ClearConfigWhenChangeIT 
extends AbstractLdapIntegrationIT
{
    
    private XStream xstream;

    private MediaType mediaType;

    public Nexus3244ClearConfigWhenChangeIT()
    {
        super();
        this.xstream = this.getJsonXStream();
        this.mediaType = MediaType.APPLICATION_JSON;
    }
    
    @Test
    public void testConfigIsUpdatedWhenChanged()
        throws Exception
    {   
        // start with good configuration
        Assert.assertEquals( 4, this.doSearch( "", false, "LDAP" ).size() );
        
        // mess up ldap configuration
        LdapUserGroupMessageUtil userGroupUtil = new LdapUserGroupMessageUtil( xstream, mediaType );
        LdapUserAndGroupConfigurationDTO userGroupConfig = userGroupUtil.getUserGroupConfig();
        String originalIdAttribute = userGroupConfig.getUserIdAttribute();
        userGroupConfig.setUserIdAttribute( "JUNKINVALIDJUNK" );
        userGroupUtil.updateUserGroupConfig( userGroupConfig );

        Assert.assertEquals( 0, this.doSearch( "", false, "LDAP" ).size() );
        
        // change config back to correct state
        userGroupConfig.setUserIdAttribute( originalIdAttribute );
        userGroupUtil.updateUserGroupConfig( userGroupConfig );
        Assert.assertEquals( 4, this.doSearch( "", false, "LDAP" ).size() );
    }

    
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
    
}
