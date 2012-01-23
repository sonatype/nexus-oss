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
package org.sonatype.security.ldap.usermanagement;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Test;
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
        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/conf/security-configuration-no-ldap.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security.xml" ) ) );

        IOUtil.copy( getClass().getResourceAsStream( "/test-conf/conf/security-configuration.xml" ),
            new FileOutputStream( new File( CONF_HOME, "security-configuration.xml" ) ) );

        // IOUtil.copy(
        // getClass().getResourceAsStream( "/test-conf/conf/ldap.xml" ),
        // new FileOutputStream( new File( CONF_HOME, "ldap.xml" ) ) );

        super.setUp();
    }

    @Test
    public void testNotConfigured()
        throws Exception
    {
        UserManager userManager = this.lookup( UserManager.class, "LDAP" );
        Assert.assertNull( userManager.getUser( "cstamas" ) );
    }
}
