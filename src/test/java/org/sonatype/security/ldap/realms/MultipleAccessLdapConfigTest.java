/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms;

import java.io.File;

import javax.naming.NamingException;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.jsecurity.realm.ldap.LdapContextFactory;
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
        connectionInfo.setPort( 12345 );
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
