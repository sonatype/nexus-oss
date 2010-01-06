/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.usermanagement;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.usermanagement.UserManager;

public class LdapUserManagerNotConfiguredTest
    extends AbstractLdapTestEnvironment
{

    public static final String SECURITY_CONFIG_KEY = "security-xml-file";

    public static final String LDAP_CONFIGURATION_KEY = "application-conf";

    protected static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );

    protected static final File CONF_HOME = new File( PLEXUS_HOME, "conf" );

    @Override
    protected void customizeContext( Context ctx )
    {
        ctx.put( SECURITY_CONFIG_KEY, new File( CONF_HOME, "security.xml" ).getAbsolutePath() );
        ctx.put( LDAP_CONFIGURATION_KEY, CONF_HOME.getAbsolutePath() );
    }

    @Override
    public void setUp()
        throws Exception
    {
        FileUtils.deleteDirectory( CONF_HOME );
        CONF_HOME.mkdirs();
        IOUtil.copy(
            getClass().getResourceAsStream( "/test-conf/conf/security-configuration-no-ldap.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security.xml" ) ) );

        IOUtil.copy(
            getClass().getResourceAsStream( "/test-conf/conf/security-configuration.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security-configuration.xml" ) ) );

        // IOUtil.copy(
        // getClass().getResourceAsStream( "/test-conf/conf/ldap.xml" ),
        // new FileOutputStream( new File( CONF_HOME, "ldap.xml" ) ) );

        super.setUp();
    }

    public void testNotConfigured()
        throws Exception
    {
        UserManager userManager = this.lookup( UserManager.class, "LDAP" );
        Assert.assertNull( userManager.getUser( "cstamas" ) );
    }
}
