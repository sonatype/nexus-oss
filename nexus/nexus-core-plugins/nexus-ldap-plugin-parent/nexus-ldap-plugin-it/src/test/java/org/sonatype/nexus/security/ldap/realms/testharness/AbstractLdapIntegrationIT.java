/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.security.ldap.realms.testharness;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.security.ldap.realms.api.XStreamInitalizer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractLdapIntegrationIT
    extends AbstractNexusIntegrationTest
{
    public static final String LDIF_DIR = "../../ldif_dir";

    private LdapServer ldapServer;

    public AbstractLdapIntegrationIT()
    {

    }

    @BeforeClass
    public void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        this.copyConfigFile( "test.ldif", LDIF_DIR );

        // copy ldap.xml to work dir
        Map<String, String> interpolationMap = new HashMap<String, String>();
        interpolationMap.put( "port", Integer.toString( this.getLdapPort() ) );

        this.copyConfigFile( "ldap.xml", interpolationMap, WORK_CONF_DIR );

    }

    protected boolean deleteLdapConfig()
    {
        File ldapConfig = new File( WORK_CONF_DIR, "ldap.xml" );
        if ( ldapConfig.exists() )
        {
            return ldapConfig.delete();
        }
        return true;
    }

    protected int getLdapPort()
    {
        if ( this.ldapServer == null )
        {
            try
            {
                beforeLdapTests();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                Assert.fail( "Failed to initilize ldap server: " + e.getMessage() );
            }
        }
        return this.ldapServer.getPort();
    }

    @BeforeMethod
    public void beforeLdapTests()
        throws Exception
    {
        if ( this.ldapServer == null )
        {
            this.ldapServer = lookupLdapServer();
        }

        if ( !this.ldapServer.isStarted() )
        {
            this.ldapServer.start();
        }
    }

    protected LdapServer lookupLdapServer()
        throws ComponentLookupException
    {
        return lookup( LdapServer.class );
    }

    @AfterMethod
    public void afterLdapTests()
        throws Exception
    {
        if ( this.ldapServer != null )
        {
            this.ldapServer.stop();
        }
    }

    @Override
    public XStream getXMLXStream()
    {
        return new XStreamInitalizer().initXStream( super.getXMLXStream() );
    }

    @Override
    public XStream getJsonXStream()
    {
        return new XStreamInitalizer().initXStream( super.getJsonXStream() );
    }
}
