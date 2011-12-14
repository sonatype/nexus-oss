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
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.record.AbstractFeedRecorderEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeChanged;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.plexus.appevents.Event;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryEventProxyModeChanged" )
public class RepositoryEventProxyModeChangedInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{

    public boolean accepts( Event<?> evt )
    {
        if ( evt instanceof RepositoryEventProxyModeChanged )
        {
            return true;
        }
        return false;
    }

    public void inspect( Event<?> evt )
    {
        RepositoryEventProxyModeChanged revt = (RepositoryEventProxyModeChanged) evt;

        StringBuffer sb = new StringBuffer( "The proxy mode of repository '" );

        sb.append( revt.getRepository().getName() );

        sb.append( "' (ID='" ).append( revt.getRepository().getId() ).append( "') was set to " );

        if ( ProxyMode.ALLOW.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Allow." );
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Blocked (auto)." );
        }
        else if ( ProxyMode.BLOCKED_MANUAL.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Blocked (by user)." );
        }
        else
        {
            sb.append( revt.getRepository().getProxyMode().toString() ).append( "." );
        }

        sb.append( " The previous state was " );

        if ( ProxyMode.ALLOW.equals( revt.getOldProxyMode() ) )
        {
            sb.append( "Allow." );
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( revt.getOldProxyMode() ) )
        {
            sb.append( "Blocked (auto)." );
        }
        else if ( ProxyMode.BLOCKED_MANUAL.equals( revt.getOldProxyMode() ) )
        {
            sb.append( "Blocked (by user)." );
        }
        else
        {
            sb.append( revt.getOldProxyMode().toString() ).append( "." );
        }

        if ( revt.getCause() != null )
        {
            sb.append( " Last detected transport error: " ).append( revt.getCause().getMessage() );
        }

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_REPO_PSTATUS_CHANGES_ACTION, sb.toString() );
    }

}
