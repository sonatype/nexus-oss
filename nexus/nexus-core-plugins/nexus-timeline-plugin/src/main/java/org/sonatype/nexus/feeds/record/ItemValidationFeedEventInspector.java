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
package org.sonatype.nexus.feeds.record;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailed;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailedChecksum;
import org.sonatype.nexus.proxy.events.RepositoryItemValidationEventFailedFileType;
import org.sonatype.plexus.appevents.Event;

/**
 * Event inspector that creates feeds about failed item validations.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ItemValidationFeedEventInspector" )
public class ItemValidationFeedEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{
    public boolean accepts( final Event<?> evt )
    {
        if ( evt instanceof RepositoryItemValidationEventFailed )
        {
            return true;
        }

        return false;
    }

    public void inspect( final Event<?> evt )
    {
        final RepositoryItemValidationEventFailed ievt = (RepositoryItemValidationEventFailed) evt;

        final NexusItemInfo ai = new NexusItemInfo();
        ai.setRepositoryId( ievt.getItem().getRepositoryId() );
        ai.setPath( ievt.getItem().getPath() );
        ai.setRemoteUrl( ievt.getItem().getRemoteUrl() );

        String action = NexusArtifactEvent.ACTION_BROKEN;

        if ( ievt instanceof RepositoryItemValidationEventFailedChecksum )
        {
            action = NexusArtifactEvent.ACTION_BROKEN_WRONG_REMOTE_CHECKSUM;
        }
        else if ( ievt instanceof RepositoryItemValidationEventFailedFileType )
        {
            action = NexusArtifactEvent.ACTION_BROKEN_INVALID_CONTENT;
        }

        final NexusArtifactEvent nae = new NexusArtifactEvent( ievt.getEventDate(), action, ievt.getMessage(), ai );

        getFeedRecorder().addNexusArtifactEvent( nae );
    }
}
