/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.ldap.realms.testharness;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import org.junit.After;
import org.junit.Before;
import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.security.ldap.realms.api.XStreamInitalizer;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractLdapIntegrationIT
    extends AbstractNexusIntegrationTest
{
    public static final String LDIF_DIR = "../../ldif_dir";

    private LdapServer ldapServer;

    public AbstractLdapIntegrationIT()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();

        // copy ldap.xml to work dir
        this.copyConfigFile( "ldap.xml", WORK_CONF_DIR );

        this.copyConfigFile( "test.ldif", LDIF_DIR );
    }

    protected boolean deleteLdapConfig()
    {
        File ldapConfig = new File( WORK_CONF_DIR , "ldap.xml" );
        if ( ldapConfig.exists() )
        {
            return ldapConfig.delete();
        }
        return true;
    }

    @Before
    public void beforeLdapTests()
        throws Exception
    {
        this.ldapServer = (LdapServer) this.lookup( LdapServer.ROLE );
        if ( !this.ldapServer.isStarted() )
        {
            this.ldapServer.start();
        }

    }

    @After
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
