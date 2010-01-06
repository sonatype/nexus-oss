/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserListResponse;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapUserResponseDTO;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.rest.model.UserToRoleResource;
import org.sonatype.security.rest.model.UserToRoleResourceRequest;

import com.thoughtworks.xstream.XStream;

public class LdapUsersMessageUtil
{

    private static final String SERVICE_PART = RequestFacade.SERVICE_LOCAL + "user_to_roles";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( LdapUsersMessageUtil.class );

    public LdapUsersMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public List<LdapUserResponseDTO> getLdapUsers()
    throws IOException
    {
        Response response = this.sendMessage( Method.GET, null, null );

        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        LdapUserListResponse resourceResponse = (LdapUserListResponse) representation
            .getPayload( new LdapUserListResponse() );

        return resourceResponse.getLdapUserRoleMappings();

    }

    public Response updateLdapUser( UserToRoleResource resource, String source )
        throws Exception
    {
        return this.sendMessage( Method.PUT, resource, source );
    }

    public Response sendMessage( Method method, UserToRoleResource resource, String source )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = SERVICE_PART + "/" + source + "/"+ resource.getUserId();

        UserToRoleResourceRequest repoResponseRequest = new UserToRoleResourceRequest();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        LOG.debug( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    @SuppressWarnings( "unchecked" )
    public void validateLdapConfig( UserToRoleResource userToRole )
        throws Exception
    {
        List<CUserRoleMapping> mapping = SecurityConfigUtil.getSecurityConfig().getUserRoleMappings();
        for ( CUserRoleMapping userRoleMapping : mapping )
        {
            if( userToRole.getUserId().equals( userRoleMapping.getUserId() ))
            {
                Assert.assertEquals( userToRole.getRoles(), userRoleMapping.getRoles() );
                return;
            }
        }
        Assert.fail( "User:  "+ userToRole.getUserId() +" not found in config file." );

    }

    public void validateResourceResponse( UserToRoleResource expected, CUserRoleMapping actual )
        throws Exception
    {
        Assert.assertEquals( expected.getUserId(), actual.getUserId() );
        Assert.assertEquals( expected.getRoles(), actual.getRoles() );

        // also validate the file config
        this.validateLdapConfig( expected );
    }

}
