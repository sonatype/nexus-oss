/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.ldap.realms.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;
import org.sonatype.nexus.AbstractNexusLdapTestCase;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoResponse;
import org.sonatype.plexus.rest.resource.PlexusResource;


public class LdapConnMd5Test
    extends AbstractNexusLdapTestCase
{

    private PlexusResource getResource()
        throws Exception
    {
        return this.lookup( PlexusResource.class, "LdapConnectionInfoPlexusResource" );
    }

    @Test
    public void testPutChangeConfig()
        throws Exception
    {
        // the test config starts off being setup for simple auth scheme, this test will update it for DIGEST-MD5
        PlexusResource resource = getResource();

        LdapConnectionInfoResponse response = new LdapConnectionInfoResponse();
        LdapConnectionInfoDTO connectionInfo = new LdapConnectionInfoDTO();
        response.setData( connectionInfo );
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( this.getLdapPort() );
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
        InputStream in = getClass().getResourceAsStream( "/test-conf/md5-ldap.xml" );
        this.interpolateLdapXml( in, new File( getNexusLdapConfiguration() ) );
        IOUtil.close( in );
    }

}
