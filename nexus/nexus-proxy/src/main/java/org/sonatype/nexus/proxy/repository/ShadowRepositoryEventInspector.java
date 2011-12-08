/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.plexus.appevents.Event;

/**
 * A "relaying" event inspector that is made asynchronous and is used to relay the repository content change related
 * events to ShadowRepository instances present in system.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ShadowRepositoryEventInspector" )
public class ShadowRepositoryEventInspector
    extends AbstractEventInspector
    implements EventInspector, AsynchronousEventInspector
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
                if ( shadow.getMasterRepositoryId().equals( ievt.getRepository().getId() ) )
                {
                    shadow.onRepositoryItemEvent( ievt );
                }
            }
        }
    }
}
