/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import java.io.File;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.LdapRealmPlexusResourceConst;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;


public class LdapConnNotConfiguredTest
    extends AbstractNexusTestCase
{

    private PlexusResource getResource() throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapConnectionInfoPlexusResource" );
    }

    public void testGetNotConfigured() throws Exception
    {
        PlexusResource resource = getResource();

        // none of these args are used, but if they start being used, we will need to change this.
        LdapConnectionInfoResponse response = (LdapConnectionInfoResponse) resource.get( null, null, null, null );

        // asssert an empty data is returned
        Assert.assertEquals( new LdapConnectionInfoDTO(), response.getData() );
    }

    public void testPutNotConfigured() throws Exception
    {
        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = new LdapConnectionInfoDTO();
        response.setData( connectionInfo );
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( 12345 );
        connectionInfo.setSearchBase( "o=sonatype" );
        connectionInfo.setSystemPassword( "secret" );
        connectionInfo.setSystemUsername( "uid=admin,ou=system" );
        connectionInfo.setProtocol( "ldap" );
        connectionInfo.setAuthScheme( "simple" );

        LdapConnectionInfoResponse result = (LdapConnectionInfoResponse) resource.put( null, null, null, response );
        this.validateConnectionDTO(connectionInfo, result.getData());

        // now how about that get
        result = (LdapConnectionInfoResponse) resource.get( null, null, null, null);
        this.validateConnectionDTO(connectionInfo, result.getData());
    }


    public void testSetPasswordToFake() throws Exception
    {

        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = new LdapConnectionInfoDTO();
        response.setData( connectionInfo );
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( 12345 );
        connectionInfo.setSearchBase( "o=sonatype" );
        connectionInfo.setSystemPassword( LdapRealmPlexusResourceConst.FAKE_PASSWORD );
        connectionInfo.setSystemUsername( "uid=admin,ou=system" );
        connectionInfo.setProtocol( "ldap" );
        connectionInfo.setAuthScheme( "simple" );

        //this is the same as not setting the password so it should throw an exception

        try
        {
            resource.put( null, null, null, response );
            Assert.fail( "Expected PlexusResourceException" );
        }
        catch(PlexusResourceException e )
        {
            ErrorResponse errorResponse = (ErrorResponse) e.getResultObject();
            Assert.assertEquals( 1, errorResponse.getErrors().size() );

         Assert.assertTrue( this.getErrorString( errorResponse, 0 ).toLowerCase().contains( "password" ));
        }
    }



    public void testGetPasswordNullWhenNotSet() throws Exception
    {
        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = new LdapConnectionInfoDTO();
        response.setData( connectionInfo );
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( 12345 );
        connectionInfo.setSearchBase( "o=sonatype" );
//        connectionInfo.setSystemPassword( "secret" );
//        connectionInfo.setSystemUsername( "uid=admin,ou=system" );
        connectionInfo.setProtocol( "ldap" );
        connectionInfo.setAuthScheme( "none" );

        LdapConnectionInfoResponse result = (LdapConnectionInfoResponse) resource.put( null, null, null, response );
        this.validateConnectionDTO(connectionInfo, result.getData());

        // now how about that get
        result = (LdapConnectionInfoResponse) resource.get( null, null, null, null);
        this.validateConnectionDTO(connectionInfo, result.getData());
    }


    /* (non-Javadoc)
     * @see com.sonatype.nexus.AbstractNexusTestCase#customizeContext(org.codehaus.plexus.context.Context)
     */
    @Override
    protected void customizeContext( Context ctx )
    {
        super.customizeContext( ctx );

        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath()+"/no-conf/" );
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();

        // delete the ldap.xml file
        File confFile = new File(CONF_HOME.getAbsolutePath()+"/no-conf/", "ldap.xml");
        confFile.delete();
    }

}
