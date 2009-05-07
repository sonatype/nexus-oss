/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.upgrades.nexus652;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;

/**
 * Test nexus.xml after and upgrade from 1.0.0-beta-5 to 1.0.0.
 */
public class Nexus652Beta5To10UpgradeTest
    extends AbstractNexusIntegrationTest
{

    public Nexus652Beta5To10UpgradeTest()
    {
        this.setVerifyNexusConfigBeforeStart( false );

        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void checkNexusConfig()
        throws IOException
    {
        // if we made it this far the upgrade worked...

        Configuration nexusConfig = NexusConfigUtil.getNexusConfig();

        Assert.assertEquals( "Smtp host:", "foo.org", nexusConfig.getSmtpConfiguration().getHostname() );
        Assert.assertEquals( "Smtp password:", "now", nexusConfig.getSmtpConfiguration().getPassword() );
        Assert.assertEquals( "Smtp username:", "void", nexusConfig.getSmtpConfiguration().getUsername() );
        Assert.assertEquals( "Smtp port:", 465, nexusConfig.getSmtpConfiguration().getPort() );

        Assert.assertEquals( "Security anon username:", "User3", nexusConfig.getSecurity().getAnonymousUsername() );
        Assert.assertEquals( "Security anon password:", "y6i0t9q1e3", nexusConfig.getSecurity().getAnonymousPassword() );
        Assert.assertEquals( "Security anon access:", true, nexusConfig.getSecurity().isAnonymousAccessEnabled() );
        Assert.assertEquals( "Security enabled:", true, nexusConfig.getSecurity().isEnabled() );
        Assert.assertEquals( "Security realm size:", 2, nexusConfig.getSecurity().getRealms().size() );
        Assert.assertEquals( "Security realm:", "XmlAuthenticatingRealm", nexusConfig.getSecurity().getRealms().get( 0 ) );
        Assert.assertEquals( "Security realm:", "XmlAuthorizingRealm", nexusConfig.getSecurity().getRealms().get( 1 ) );

        Assert.assertEquals( "http proxy:", true, nexusConfig.getHttpProxy().isEnabled() );

        Assert.assertEquals( "Base url:", AbstractNexusIntegrationTest.baseNexusUrl,
                             nexusConfig.getRestApi().getBaseUrl() );

        // we will glance over the repos, because the unit tests cover this.
        Assert.assertEquals( "Repository Count:", 9, nexusConfig.getRepositories().size() );

        Assert.assertNotNull( "repo: central", NexusConfigUtil.getRepo( "central" ) );
        Assert.assertNotNull( "repo: apache-snapshots", NexusConfigUtil.getRepo( "apache-snapshots" ) );
        Assert.assertNotNull( "repo: codehaus-snapshots", NexusConfigUtil.getRepo( "codehaus-snapshots" ) );
        Assert.assertNotNull( "repo: releases", NexusConfigUtil.getRepo( "releases" ) );
        Assert.assertNotNull( "repo: snapshots", NexusConfigUtil.getRepo( "snapshots" ) );
        Assert.assertNotNull( "repo: thirdparty", NexusConfigUtil.getRepo( "thirdparty" ) );

        // everything else including everything above should be covered by unit tests.

    }


    @Test
    public void checkSecurityConfig()
        throws IOException
    {
        org.sonatype.jsecurity.model.Configuration secConfig = SecurityConfigUtil.getSecurityConfig();

        Assert.assertEquals( "User Count:", 7, secConfig.getUsers().size());
        Assert.assertEquals( "Roles Count:", 22, secConfig.getRoles().size());

        // again, everything should have been upgraded.
    }

}
