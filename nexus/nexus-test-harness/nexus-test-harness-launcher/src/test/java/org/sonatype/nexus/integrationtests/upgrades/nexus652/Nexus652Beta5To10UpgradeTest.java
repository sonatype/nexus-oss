package org.sonatype.nexus.integrationtests.upgrades.nexus652;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.nexus.test.utils.SecurityConfigUtil;
import org.sonatype.nexus.test.utils.TestProperties;

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

        Assert.assertEquals( "Smtp host:", "foo.org", nexusConfig.getSmtpConfiguration().getHost() );
        Assert.assertEquals( "Smtp password:", "now", nexusConfig.getSmtpConfiguration().getPassword() );
        Assert.assertEquals( "Smtp username:", "void", nexusConfig.getSmtpConfiguration().getUsername() );
        Assert.assertEquals( "Smtp port:", 465, nexusConfig.getSmtpConfiguration().getPort() );

        Assert.assertEquals( "Security anon username:", "User3", nexusConfig.getSecurity().getAnonymousUsername() );
        Assert.assertEquals( "Security anon password:", "y6i0t9q1e3", nexusConfig.getSecurity().getAnonymousPassword() );
        Assert.assertEquals( "Security anon access:", true, nexusConfig.getSecurity().isAnonymousAccessEnabled() );
        Assert.assertEquals( "Security enabled:", true, nexusConfig.getSecurity().isEnabled() );
        Assert.assertEquals( "Security realm size:", 1, nexusConfig.getSecurity().getRealms().size() );
        Assert.assertEquals( "Security realm:", "NexusTargetRealm", nexusConfig.getSecurity().getRealms().get( 0 ) );

        Assert.assertEquals( "http proxy:", true, nexusConfig.getHttpProxy().isEnabled() );

        Assert.assertEquals( "Base url:", TestProperties.getString( "nexus.base.url" ),
                             nexusConfig.getRestApi().getBaseUrl() );

        // we will glance over the repos, because the unit tests cover this.
        Assert.assertEquals( "Repository Count:", 6, nexusConfig.getRepositories().size() );
        Assert.assertEquals( "Repository Shadow Count:", 1, nexusConfig.getRepositoryShadows().size() );

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
        Assert.assertEquals( "Roles Count:", 4, secConfig.getRoles().size());
        
        // again, everything should have been upgraded.
    }

}
