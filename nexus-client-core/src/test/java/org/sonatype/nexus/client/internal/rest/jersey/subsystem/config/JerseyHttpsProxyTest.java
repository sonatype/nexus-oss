/*
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
package org.sonatype.nexus.client.internal.rest.jersey.subsystem.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.sonatype.nexus.client.core.subsystem.config.HttpProxy;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClient;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

/**
 * @since 2.5
 */
public class JerseyHttpsProxyTest
    extends TestSupport
{

    private GlobalConfigurationResource configuration = new GlobalConfigurationResource();

    @Test
    public void noSettings()
    {
        final HttpProxy underTest = createJerseyHttpsProxy();
        final RemoteHttpProxySettings settings = underTest.settings();

        assertThat( settings, is( notNullValue() ) );

        settings.setProxyHostname( "bar" );
        underTest.save();

        assertThat( configuration.getGlobalHttpsProxySettings(), is( notNullValue() ) );
        assertThat( configuration.getGlobalHttpsProxySettings().getProxyHostname(), is( "bar" ) );
    }

    @Test
    public void existingSettings()
    {
        final RemoteHttpProxySettings configSettings = new RemoteHttpProxySettings();
        configSettings.setProxyHostname( "foo" );
        configuration.setGlobalHttpsProxySettings( configSettings );

        final HttpProxy underTest = createJerseyHttpsProxy();
        final RemoteHttpProxySettings settings = underTest.settings();

        assertThat( settings, is( notNullValue() ) );
        assertThat( settings.getProxyHostname(), is( "foo" ) );

        settings.setProxyHostname( "bar" );
        underTest.save();

        assertThat( configuration.getGlobalHttpsProxySettings(), is( notNullValue() ) );
        assertThat( configuration.getGlobalHttpsProxySettings().getProxyHostname(), is( "bar" ) );
    }

    @Test
    public void reset()
    {
        final HttpProxy underTest = createJerseyHttpsProxy();
        final RemoteHttpProxySettings settings = underTest.settings();

        assertThat( settings, is( notNullValue() ) );

        settings.setProxyHostname( "bar" );
        underTest.refresh();

        assertThat( settings.getProxyHostname(), is( nullValue() ) );
    }

    @Test
    public void disable()
    {
        final RemoteHttpProxySettings configSettings = new RemoteHttpProxySettings();
        configSettings.setProxyHostname( "foo" );
        configuration.setGlobalHttpsProxySettings( configSettings );

        final HttpProxy underTest = createJerseyHttpsProxy();

        underTest.disable();

        assertThat( configuration.getGlobalHttpsProxySettings(), is( nullValue() ) );
    }

    private JerseyHttpsProxy createJerseyHttpsProxy()
    {
        return new JerseyHttpsProxy( mock( JerseyNexusClient.class ) )
        {
            @Override
            GlobalConfigurationResource getConfiguration()
            {
                return configuration;
            }

            @Override
            void setConfiguration( final GlobalConfigurationResource configuration )
            {
                // do nothing
            }

        };
    }

}
