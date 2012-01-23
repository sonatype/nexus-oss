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
package org.sonatype.nexus.integrationtests.upgrades.nexus652;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.configuration.source.SecurityConfigurationSource;
import org.sonatype.security.model.CRole;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test nexus.xml after and upgrade from 1.0.0-beta-5 to 1.0.0.
 */
public class Nexus652Beta5To10UpgradeIT
    extends AbstractNexusIntegrationTest
{

    @BeforeClass
    public void setSecureTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void checkNexusConfig()
        throws Exception
    {
        // if we made it this far the upgrade worked...

        SecurityConfigurationSource securitySource = lookup( SecurityConfigurationSource.class, "file" );
        SecurityConfiguration securityConfig = securitySource.loadConfiguration();

        // we need this to have access to uncrypted password (see assertion below)
        Configuration nexusConfig = getNexusConfigUtil().loadAndUpgradeNexusConfiguration();

        Assert.assertEquals( nexusConfig.getSmtpConfiguration().getHostname(), "foo.org", "Smtp host:" );
        Assert.assertEquals( nexusConfig.getSmtpConfiguration().getPassword(), "now", "Smtp password:" );
        Assert.assertEquals( nexusConfig.getSmtpConfiguration().getUsername(), "void", "Smtp username:" );
        Assert.assertEquals( nexusConfig.getSmtpConfiguration().getPort(), 465, "Smtp port:" );

        Assert.assertEquals( securityConfig.getAnonymousUsername(), "User3", "Security anon username:" );
        Assert.assertEquals( securityConfig.getAnonymousPassword(), "y6i0t9q1e3", "Security anon password:" );
        Assert.assertEquals( securityConfig.isAnonymousAccessEnabled(), true, "Security anon access:" );
        Assert.assertEquals( securityConfig.isEnabled(), true, "Security enabled:" );
        Assert.assertEquals( securityConfig.getRealms().size(), 2, "Security realm size:" );
        Assert.assertEquals( securityConfig.getRealms().get( 0 ), "XmlAuthenticatingRealm", "Security realm:" );
        Assert.assertEquals( securityConfig.getRealms().get( 1 ), "XmlAuthorizingRealm", "Security realm:" );

        Assert.assertEquals( nexusConfig.getHttpProxy().isEnabled(), true, "http proxy:" );

        Assert.assertEquals( nexusConfig.getRestApi().getBaseUrl(), AbstractNexusIntegrationTest.nexusBaseUrl,
                             "Base url:" );

        // we will glance over the repos, because the unit tests cover this.
        Assert.assertEquals( nexusConfig.getRepositories().size(), 9, "Repository Count:" );

        Assert.assertNotNull( getNexusConfigUtil().getRepo( "central" ), "repo: central" );
        Assert.assertNotNull( getNexusConfigUtil().getRepo( "apache-snapshots" ), "repo: apache-snapshots" );
        Assert.assertNotNull( getNexusConfigUtil().getRepo( "codehaus-snapshots" ), "repo: codehaus-snapshots" );
        Assert.assertNotNull( getNexusConfigUtil().getRepo( "releases" ), "repo: releases" );
        Assert.assertNotNull( getNexusConfigUtil().getRepo( "snapshots" ), "repo: snapshots" );
        Assert.assertNotNull( getNexusConfigUtil().getRepo( "thirdparty" ), "repo: thirdparty" );

        // everything else including everything above should be covered by unit tests.

    }

    @Test
    public void checkSecurityConfig()
        throws IOException
    {
        org.sonatype.security.model.Configuration secConfig = getSecurityConfigUtil().getSecurityConfig();

        Assert.assertEquals( secConfig.getUsers().size(), 7, "User Count:" );
        List<String> roleIds = new ArrayList<String>();
        for ( CRole role : secConfig.getRoles() )
        {
            roleIds.add( role.getId() );
        }
        Assert.assertEquals( secConfig.getRoles().size(), 29, "Roles Count differs, expected: 29, found: " + roleIds );

        // again, everything should have been upgraded.
    }

}
