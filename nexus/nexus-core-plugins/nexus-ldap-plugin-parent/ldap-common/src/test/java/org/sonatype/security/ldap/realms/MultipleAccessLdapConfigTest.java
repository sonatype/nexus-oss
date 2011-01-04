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
package org.sonatype.security.ldap.realms;

import java.io.File;

import javax.naming.NamingException;

import junit.framework.Assert;

import org.codehaus.plexus.context.Context;
import org.apache.shiro.realm.ldap.LdapContextFactory;
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
