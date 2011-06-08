/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.updatesite;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventExpireCaches;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "RepositoryUrlChangeEventListener" )
public class RepositoryUpdateMirrorEventListener
    extends AbstractEventInspector
    implements EventInspector
{

    public static final String TASKNAME_MIRROR_ECLIPSE_SITE = "Mirror Eclipse Update Site";

    @Requirement
    private NexusScheduler scheduler;

    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryConfigurationUpdatedEvent || evt instanceof RepositoryEventExpireCaches;
    }

    public void inspect( Event<?> evt )
    {
        Repository repository = ( (RepositoryEvent) evt ).getRepository();

        if ( repository instanceof UpdateSiteRepository
            && ( evt instanceof RepositoryEventExpireCaches || ( (RepositoryConfigurationUpdatedEvent) evt ).isRemoteUrlChanged() ) )
        {
            UpdateSiteMirrorTask mirrorTask = scheduler.createTaskInstance( UpdateSiteMirrorTask.class );
            mirrorTask.setRepositoryId( repository.getId() );
            scheduler.submit( TASKNAME_MIRROR_ECLIPSE_SITE, mirrorTask );
            getLogger().debug( "Submitted " + TASKNAME_MIRROR_ECLIPSE_SITE );
        }
    }
}
