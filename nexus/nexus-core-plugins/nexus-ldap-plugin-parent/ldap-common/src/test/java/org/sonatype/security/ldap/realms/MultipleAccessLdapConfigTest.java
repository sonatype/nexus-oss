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
package org.sonatype.security.ldap.realms;

import java.io.File;

import javax.naming.NamingException;

import junit.framework.Assert;

import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.codehaus.plexus.context.Context;
import org.junit.Test;
import org.sonatype.ldaptestsuite.AbstractLdapTestEnvironment;
import org.sonatype.security.ldap.realms.persist.InvalidConfigurationException;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;
import org.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

public class MultipleAccessLdapConfigTest
    extends AbstractLdapTestEnvironment
{

    private LdapContextFactory ldapContextFactory;

    private LdapConfiguration ldapConfig;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        ldapContextFactory = this.lookup( LdapContextFactory.class, "PlexusLdapContextFactory" );
        ldapConfig = this.lookup( LdapConfiguration.class );
    }

    @Override
    protected void customizeContext( Context context )
    {
        context.put( "application-conf", getBasedir() + "/target/test-classes/not-configured/" );
    }

    @Test
    public void testConfigure()
        throws InvalidConfigurationException, NamingException
    {
        try
        {
            ldapContextFactory.getSystemLdapContext();
            Assert.fail( "Expected NamingException" );
        }
        catch ( NamingException e )
        {
            // expected
        }

        // now configure the relam
        CConnectionInfo connectionInfo = new CConnectionInfo();
        connectionInfo.setHost( "localhost" );
        connectionInfo.setPort( this.getLdapServer().getPort() );
        connectionInfo.setAuthScheme( "none" );
        connectionInfo.setSearchBase( "o=sonatype" );
        connectionInfo.setProtocol( "ldap" );

        ldapConfig.updateConnectionInfo( connectionInfo );
        ldapConfig.save();

        // now we should be able to get a valid configuration
        ldapContextFactory.getSystemLdapContext();

    }

    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();

        // delete the ldap.xml file
        File confFile = new File( getBasedir() + "/target/test-classes/not-configured/", "ldap.xml" );
        confFile.delete();
    }

}
