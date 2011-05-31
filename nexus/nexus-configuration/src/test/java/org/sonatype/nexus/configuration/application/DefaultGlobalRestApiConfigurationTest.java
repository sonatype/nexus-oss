package org.sonatype.nexus.configuration.application;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public class DefaultGlobalRestApiConfigurationTest
    extends AbstractNexusTestCase
{

    @Test
    public void testNoConfiguration()
        throws ConfigurationException
    {
        DefaultGlobalRestApiSettings settings = new DefaultGlobalRestApiSettings();
        SimpleApplicationConfiguration cfg = new SimpleApplicationConfiguration();
        cfg.getConfigurationModel().setRestApi( null );
        settings.configure( cfg );

        Assert.assertNull( settings.getBaseUrl() );
        Assert.assertEquals( 0, settings.getUITimeout() );

        settings.setUITimeout( 1000 );
        settings.setBaseUrl( "http://invalid.url" );
        Assert.assertTrue( settings.commitChanges() );

        Assert.assertEquals( "http://invalid.url", settings.getBaseUrl() );
        Assert.assertEquals( 1000, settings.getUITimeout() );
    }

}
