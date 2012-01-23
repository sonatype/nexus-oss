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
package org.sonatype.nexus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.junit.Assert;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.security.ldap.realms.api.LdapRealmPlexusResourceConst;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

public abstract class AbstractNexusLdapTestCase
    extends NexusTestSupport
{

    /**
     * The ldap server.
     */
    private LdapServer ldapServer;

    protected String getNexusLdapConfiguration()
    {
        return getConfHomeDir() + "/ldap.xml";
    }

    @Override
    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        copyResource( "/test-conf/security-configuration.xml", getSecurityConfiguration() );
        copyResource( "/test-conf/security.xml", getNexusSecurityConfiguration() );
    }

    protected void copyDefaultLdapConfigToPlace()
        throws IOException
    {
        InputStream in = getClass().getResourceAsStream( "/test-conf/ldap.xml" );
        this.interpolateLdapXml( in, new File( getNexusLdapConfiguration() ) );
        IOUtil.close( in );
    }

    protected void interpolateLdapXml( InputStream inputStream, File outputFile )
        throws IOException
    {
        HashMap<String, String> interpolationMap = new HashMap<String, String>();
        interpolationMap.put( "port", Integer.toString( this.getLdapPort() ) );

        Reader reader = new InterpolationFilterReader( new InputStreamReader( inputStream ), interpolationMap );
        OutputStream out = new FileOutputStream( outputFile );
        IOUtil.copy( reader, out );
        IOUtil.close( out );
    }

    protected int getLdapPort()
    {
        Assert.assertNotNull( "LDAP server is not initialized yet.", ldapServer );
        return ldapServer.getPort();
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // configure the logging
        SLF4JBridgeHandler.install();

        // startup the LDAP server.
        ldapServer = (LdapServer) lookup( LdapServer.ROLE );

        this.copyDefaultSecurityConfigToPlace();
        this.copyDefaultLdapConfigToPlace();
    }

    protected String getErrorString( ErrorResponse errorResponse, int index )
    {
        return ( (ErrorMessage) errorResponse.getErrors().get( index ) ).getMsg();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        ldapServer.stop();

        ldapServer = null;

        // configure the logging
        SLF4JBridgeHandler.uninstall();

        super.tearDown();
    }

    protected void validateConnectionDTO( LdapConnectionInfoDTO expected, LdapConnectionInfoDTO actual )
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
    }
}
