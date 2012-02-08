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
package org.sonatype.nexus.ahc;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.events.GlobalHttpProxySettingsChangedEvent;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteConnectionSettingsChangedEvent;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * A trivial inspector listening for configuration changes and just resetting Ahc Provider (forcing it to recreate
 * shared client, in response to possible proxy or some other affecting config change). This could be refined later, and
 * reset only in case when proxy is changed or so, but current config framework is not completed and this information
 * lacks ("what" is changed).
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "AhcProviderEventInspector" )
public class AhcProviderEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
    @Requirement
    private AhcProvider ahcProvider;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof GlobalRemoteConnectionSettingsChangedEvent )
            || ( evt instanceof GlobalHttpProxySettingsChangedEvent ) || ( evt instanceof NexusStoppedEvent );
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( ( evt instanceof GlobalRemoteConnectionSettingsChangedEvent )
            || ( evt instanceof GlobalHttpProxySettingsChangedEvent ) )
        {
            ahcProvider.reset();
        }
        else if ( evt instanceof NexusStoppedEvent )
        {
            ahcProvider.close();
        }
    }
}
