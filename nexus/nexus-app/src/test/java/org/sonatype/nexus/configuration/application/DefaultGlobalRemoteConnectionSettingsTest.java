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
package org.sonatype.nexus.configuration.application;

import junit.framework.Assert;

import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteConnectionSettingsChangedEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

public class DefaultGlobalRemoteConnectionSettingsTest
    extends AbstractNexusTestCase
{

    @SuppressWarnings( "unchecked" )
    public void testEvents()
        throws Exception
    {
        NexusConfiguration cfg = lookup( NexusConfiguration.class );
        cfg.loadConfiguration();

        final Event<GlobalRemoteConnectionSettings>[] event = new Event[1];
        ApplicationEventMulticaster applicationEventMulticaster = lookup( ApplicationEventMulticaster.class );
        applicationEventMulticaster.addEventListener( new EventListener()
        {
            public void onEvent( Event<?> evt )
            {
                if ( evt instanceof GlobalRemoteConnectionSettingsChangedEvent )
                {
                    event[0] = (GlobalRemoteConnectionSettingsChangedEvent) evt;
                }
            }
        } );

        GlobalRemoteConnectionSettings settings = lookup( GlobalRemoteConnectionSettings.class );

        settings.setConnectionTimeout( 2 );
        settings.setRetrievalRetryCount( 3 );

        cfg.saveConfiguration();

        Assert.assertNotNull( event[0].getEventSender() );
        Assert.assertEquals( settings, event[0].getEventSender() );
        Assert.assertEquals( 2, event[0].getEventSender().getConnectionTimeout() );
        Assert.assertEquals( 3, event[0].getEventSender().getRetrievalRetryCount() );

    }
}
