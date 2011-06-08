/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.updatesite;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = UpdateSiteRepository.ROLE_HINT )
public class RepositoryCreationEventListener
    implements EventInspector
{

    private boolean active;

    public boolean accepts( Event<?> evt )
    {
        active |= evt instanceof NexusStartedEvent;

        return active && evt instanceof RepositoryRegistryEventAdd;
    }

    public void inspect( Event<?> evt )
    {
        Repository repository = ( (RepositoryRegistryEventAdd) evt ).getRepository();

        if ( repository instanceof UpdateSiteRepository )
        {
            repository.setExposed( false );
            ( (UpdateSiteRepository) repository ).mirror( true );
        }
    }

}
