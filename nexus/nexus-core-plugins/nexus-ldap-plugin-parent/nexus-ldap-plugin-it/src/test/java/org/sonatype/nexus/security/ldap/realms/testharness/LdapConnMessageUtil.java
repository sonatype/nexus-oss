/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.security.ldap.realms.api.LdapRealmPlexusResourceConst;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.thoughtworks.xstream.XStream;

public class LdapConnMessageUtil
{

    private static final String SERVICE_PART = RequestFacade.SERVICE_LOCAL + "ldap/conn_info";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( GroupMessageUtil.class );

    public LdapConnMessageUtil( XStream xstream, MediaType mediaType )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    public LdapConnectionInfoDTO getConnectionInfo()
        throws IOException
    {
        Response response = this.sendMessage( Method.GET, null );
        return this.getResourceFromResponse( response );
    }

    public LdapConnectionInfoDTO updateConnectionInfo( LdapConnectionInfoDTO connInfo )
        throws Exception
    {
        Response response = this.sendMessage( Method.PUT, connInfo );

        if ( !response.getStatus().isSuccess() )
        {
            String responseText = response.getEntity().getText();
            Assert.fail( "Could not create Repository: " + response.getStatus() + ":\n" + responseText );
        }
        LdapConnectionInfoDTO responseResource = this.getResourceFromResponse( response );

        this.validateResourceResponse( connInfo, responseResource );

        return responseResource;
    }

    public Response sendMessage( Method method, LdapConnectionInfoDTO resource )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = SERVICE_PART;

        LdapConnectionInfoResponse repoResponseRequest = new LdapConnectionInfoResponse();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        LOG.debug( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, method, representation );
    }

    public Response sendTestMessage( LdapConnectionInfoDTO resource )
        throws IOException
    {

        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String serviceURI = RequestFacade.SERVICE_LOCAL + "ldap/test_auth";

        LdapConnectionInfoResponse repoResponseRequest = new LdapConnectionInfoResponse();
        repoResponseRequest.setData( resource );

        // now set the payload
        representation.setPayload( repoResponseRequest );

        LOG.debug( "sendMessage: " + representation.getText() );

        return RequestFacade.sendMessage( serviceURI, Method.PUT, representation );
    }

    public LdapConnectionInfoDTO getResourceFromResponse( Response response )
        throws IOException
    {
        String responseString = response.getEntity().getText();
        LOG.debug( " getResourceFromResponse: " + responseString );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );
        LdapConnectionInfoResponse resourceResponse = (LdapConnectionInfoResponse) representation
            .getPayload( new LdapConnectionInfoResponse() );

        return resourceResponse.getData();
    }

    @SuppressWarnings( "unchecked" )
    public void validateLdapConfig( LdapConnectionInfoDTO connInfo )
        throws Exception
    {
        CConnectionInfo fileConfig = LdapConfigurationUtil.getConfiguration().getConnectionInfo();
        Assert.assertEquals( connInfo.getAuthScheme(), fileConfig.getAuthScheme() );
        Assert.assertEquals( connInfo.getHost(), fileConfig.getHost() );
        Assert.assertEquals( connInfo.getPort(), fileConfig.getPort() );
        Assert.assertEquals( connInfo.getProtocol(), fileConfig.getProtocol() );
        Assert.assertEquals( connInfo.getRealm(), fileConfig.getRealm() );
        Assert.assertEquals( connInfo.getSearchBase(), fileConfig.getSearchBase() );
        Assert.assertEquals( connInfo.getSystemUsername(), fileConfig.getSystemUsername() );

        // if the expectedPassword == null then the actual should be null
        if ( connInfo.getSystemPassword() == null )
        {
            Assert.assertNull( fileConfig.getSystemPassword() );
        }
        else
        {
            // make sure its not clear text
            Assert.assertNotSame( connInfo.getSystemPassword(), fileConfig.getSystemPassword() );
            Assert.assertTrue( fileConfig.getSystemPassword().length() > 0 );
        }
    }

    public void validateResourceResponse( LdapConnectionInfoDTO expected, LdapConnectionInfoDTO actual )
        throws Exception
    {
        Assert.assertEquals( expected.getAuthScheme(), actual.getAuthScheme() );
        Assert.assertEquals( expected.getHost(), actual.getHost() );
        Assert.assertEquals( expected.getPort(), actual.getPort() );
        Assert.assertEquals( expected.getProtocol(), actual.getProtocol() );
        Assert.assertEquals( expected.getRealm(), actual.getRealm() );
        Assert.assertEquals( expected.getSearchBase(), actual.getSearchBase() );
        Assert.assertEquals( expected.getSystemUsername(), actual.getSystemUsername() );

        // if the expectedPassword == null then the actual should be null
        // if its anything else the actual password should be "--FAKE-PASSWORD--"
        if ( expected.getSystemPassword() == null )
        {
            Assert.assertNull( actual.getSystemPassword() );
        }
        else
        {
            Assert.assertEquals( LdapRealmPlexusResourceConst.FAKE_PASSWORD, actual.getSystemPassword() );
        }

        // also validate the file config
        this.validateLdapConfig( expected );
    }

}
