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
