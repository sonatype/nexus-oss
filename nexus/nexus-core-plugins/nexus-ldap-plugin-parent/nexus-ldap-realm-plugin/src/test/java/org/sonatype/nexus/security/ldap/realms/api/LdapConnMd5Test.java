/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;


public class LdapConnMd5Test
    extends AbstractNexusTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapConnectionInfoPlexusResource" );
    }

    public void testPutChangeConfig()
        throws Exception
    {
        // the test config starts off being setup for simple auth scheme, this test will update it for DIGEST-MD5
        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = new LdapConnectionInfoDTO();
        response.setData( connectionInfo );
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( 12345 );
        connectionInfo.setSearchBase( "o=sonatype" );
        connectionInfo.setSystemPassword( "secret" );
        connectionInfo.setSystemUsername( "admin" );
        connectionInfo.setProtocol( "ldap" );
        connectionInfo.setAuthScheme( "DIGEST-MD5" );
        connectionInfo.setRealm( "localhost" );

        LdapConnectionInfoResponse result = (LdapConnectionInfoResponse) resource.put( null, null, null, response );
        this.validateConnectionDTO( connectionInfo, result.getData() );

        // now how about that get
        result = (LdapConnectionInfoResponse) resource.get( null, null, null, null );
        this.validateConnectionDTO( connectionInfo, result.getData() );
    }

    @Override
    protected void copyDefaultLdapConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/md5-ldap.xml" ),
                     new FileOutputStream( getNexusLdapConfiguration() ) );
    }

}
