/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.security.ldap.realms.api.LdapRealmPlexusResourceConst;
import org.sonatype.nexus.security.ldap.realms.api.dto.LdapConnectionInfoDTO;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

public abstract class AbstractNexusTestCase
    extends PlexusTestCase
{
    public static final String RUNTIME_CONFIGURATION_KEY = "runtime";

    public static final String WORK_CONFIGURATION_KEY = "nexus-work";

    public static final String LDAP_CONFIGURATION_KEY = "application-conf";

    public static final String APPS_CONFIGURATION_KEY = "apps";
    public static final String SECURITY_CONFIG_KEY = "security-xml-file";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File WORK_HOME = new File( PLEXUS_HOME, "nexus-work" );

    protected static final File CONF_HOME = new File( WORK_HOME, "conf" );

    /** The ldap server. */
    private LdapServer ldapServer;

    @Override
    protected void customizeContext( Context ctx )
    {
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );

        ctx.put( WORK_CONFIGURATION_KEY, WORK_HOME.getAbsolutePath() );

        ctx.put( RUNTIME_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );

        ctx.put( SECURITY_CONFIG_KEY, this.getNexusSecurityConfiguration() );

        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() );
    }

    protected String getSecurityConfiguration()
    {
        return CONF_HOME + "/security-configuration.xml";
    }

    protected String getNexusSecurityConfiguration()
    {
        return CONF_HOME.getAbsolutePath() + "/security.xml";
    }

    protected String getNexusLdapConfiguration()
    {
        return CONF_HOME + "/ldap.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/security-configuration.xml" ), new FileOutputStream(
            getSecurityConfiguration() ) );
    }

    protected void copyDefaultSecurityConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/security.xml" ), new FileOutputStream(
            getNexusSecurityConfiguration() ) );
    }

    protected void copyDefaultLdapConfigToPlace()
    throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/ldap.xml" ), new FileOutputStream(
            getNexusLdapConfiguration() ) );
    }

    protected boolean loadConfigurationAtSetUp()
    {
        return true;
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

     // configure the logging
        SLF4JBridgeHandler.install();

        FileUtils.deleteDirectory( PLEXUS_HOME );

        PLEXUS_HOME.mkdirs();
        WORK_HOME.mkdirs();
        CONF_HOME.mkdirs();

        if ( loadConfigurationAtSetUp() )
        {
            this.copyDefaultConfigToPlace();
            this.copyDefaultSecurityConfigToPlace();
            this.copyDefaultLdapConfigToPlace();
        }

        // startup the LDAP server.
        ldapServer = (LdapServer) lookup( LdapServer.ROLE );
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
        if(expected.getSystemPassword() == null)
        {
            Assert.assertNull( actual.getSystemPassword() );
        }
        else
        {
            Assert.assertEquals( LdapRealmPlexusResourceConst.FAKE_PASSWORD, actual.getSystemPassword() );
        }
    }
}
