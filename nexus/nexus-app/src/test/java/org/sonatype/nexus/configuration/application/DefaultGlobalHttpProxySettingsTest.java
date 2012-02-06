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
package org.sonatype.nexus.configuration.application;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.events.GlobalHttpProxySettingsChangedEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

public class DefaultGlobalHttpProxySettingsTest
    extends AbstractNexusTestCase
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void testEvents()
        throws Exception
    {
        NexusConfiguration cfg = lookup( NexusConfiguration.class );
        cfg.loadConfiguration();

        final Event<GlobalHttpProxySettings>[] event = new Event[1];
        ApplicationEventMulticaster applicationEventMulticaster = lookup( ApplicationEventMulticaster.class );
        applicationEventMulticaster.addEventListener( new EventListener()
        {
            public void onEvent( Event<?> evt )
            {
                if ( evt instanceof GlobalHttpProxySettingsChangedEvent )
                {
                    event[0] = (GlobalHttpProxySettingsChangedEvent) evt;
                }
            }
        } );

        GlobalHttpProxySettings settings = lookup( GlobalHttpProxySettings.class );

        settings.setHostname( "foo.bar.com" );
        settings.setPort( 1234 );
        cfg.saveConfiguration();

        Assert.assertNotNull( event[0].getEventSender() );
        Assert.assertEquals( settings, event[0].getEventSender() );
        Assert.assertEquals( "foo.bar.com", event[0].getEventSender().getHostname() );
        Assert.assertEquals( 1234, event[0].getEventSender().getPort() );

    }
}
