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
package org.sonatype.nexus.plugins.p2.repository.updatesite;

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

    @Override
    public boolean accepts( final Event<?> evt )
    {
        return evt instanceof RepositoryConfigurationUpdatedEvent || evt instanceof RepositoryEventExpireCaches;
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        final Repository repository = ( (RepositoryEvent) evt ).getRepository();

        if ( repository instanceof UpdateSiteRepository
            && ( evt instanceof RepositoryEventExpireCaches || ( (RepositoryConfigurationUpdatedEvent) evt ).isRemoteUrlChanged() ) )
        {
            final UpdateSiteMirrorTask mirrorTask = scheduler.createTaskInstance( UpdateSiteMirrorTask.class );
            mirrorTask.setRepositoryId( repository.getId() );
            scheduler.submit( TASKNAME_MIRROR_ECLIPSE_SITE, mirrorTask );
            getLogger().debug( "Submitted " + TASKNAME_MIRROR_ECLIPSE_SITE );
        }
    }
}
