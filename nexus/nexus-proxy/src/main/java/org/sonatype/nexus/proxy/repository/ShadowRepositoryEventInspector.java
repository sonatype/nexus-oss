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
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.plexus.appevents.Event;

/**
 * A "relaying" event inspector that is made asynchronous and is used to relay the repository content change related
 * events to ShadowRepository instances present in system. Note: this event inspector should be async to not slow down
 * the request-processing cycle of it's master, but strange cases might happen then, like "delete" event flies in before
 * "create" event. That is not "deadly", but might leave shadow too easily in inconsistent state. So, leave this
 * inspector as sync until we come up with some better solution.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ShadowRepositoryEventInspector" )
public class ShadowRepositoryEventInspector
    extends AbstractEventInspector
    implements EventInspector
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryItemEvent;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryItemEvent )
        {
            final RepositoryItemEvent ievt = (RepositoryItemEvent) evt;
            final List<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet( ShadowRepository.class );

            for ( ShadowRepository shadow : shadows )
            {
                if ( shadow.getMasterRepository().getId().equals( ievt.getRepository().getId() ) )
                {
                    shadow.onRepositoryItemEvent( ievt );
                }
            }
        }
    }
}
