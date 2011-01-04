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
package org.sonatype.nexus.notification.events;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.sonatype.nexus.notification.NotificationMessage;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyMode;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.security.usermanagement.User;

public class RepositoryEventProxyModeMessage
    implements NotificationMessage
{
    private final RepositoryEventProxyMode repositoryEventProxyMode;

    private final User user;

    private final String title;

    private final String body;

    public RepositoryEventProxyModeMessage( RepositoryEventProxyMode revt, User user )
    {
        this.repositoryEventProxyMode = revt;

        this.user = user;

        // we will reuse this
        StringBuffer sb = null;

        // -- Title
        sb = new StringBuffer( "Proxy repository  \"" );

        sb.append( revt.getRepository().getName() );

        sb.append( "\" (repoId=" ).append( revt.getRepository().getId() ).append( ") was " );

        if ( ProxyMode.ALLOW.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "unblocked." );
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "auto-blocked." );
        }
        else if ( ProxyMode.BLOCKED_MANUAL.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "blocked." );
        }
        else
        {
            sb.append( revt.getRepository().getProxyMode().toString() ).append( "." );
        }

        this.title = sb.toString();

        // -- Body

        sb = new StringBuffer( "Howdy,\n\n" );

        sb.append( "the proxy mode of repository \"" );

        sb.append( revt.getRepository().getName() );

        sb.append( "\" (repoId=" ).append( revt.getRepository().getId() );

        sb.append( ", remoteUrl=" );

        sb.append( revt.getRepository().getRemoteUrl() );

        sb.append( ") was set to \n\n" );

        if ( ProxyMode.ALLOW.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Allow." );
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Blocked (automatically by Nexus). Next attempt to check remote peer health will occur in " );

            sb.append( DurationFormatUtils.formatDurationWords( revt.getRepository().getCurrentRemoteStatusRetainTime(),
                true, true )
                + "." );
        }
        else if ( ProxyMode.BLOCKED_MANUAL.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Blocked (by a user)." );
        }
        else
        {
            sb.append( revt.getNewProxyMode().toString() ).append( "." );
        }

        sb.append( "\n\nThe previous state was \n\n" );

        if ( ProxyMode.ALLOW.equals( revt.getOldProxyMode() ) )
        {
            sb.append( "Allow." );
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( revt.getOldProxyMode() ) )
        {
            sb.append( "Blocked (automatically by Nexus)." );
        }
        else if ( ProxyMode.BLOCKED_MANUAL.equals( revt.getOldProxyMode() ) )
        {
            sb.append( "Blocked (by a user)." );
        }
        else
        {
            sb.append( revt.getOldProxyMode().toString() ).append( "." );
        }

        if ( revt.getCause() != null )
        {
            sb.append( "\n\n\nLast detected transport error was:\n\n" );

            final Writer result = new StringWriter();

            final PrintWriter printWriter = new PrintWriter( result );

            revt.getCause().printStackTrace( printWriter );

            sb.append( result.toString() );
        }

        this.body = sb.toString();
    }

    public RepositoryEventProxyMode getRepositoryEventProxyMode()
    {
        return repositoryEventProxyMode;
    }

    public User getUser()
    {
        return user;
    }

    public String getMessageTitle()
    {
        return title;
    }

    public String getMessageBody()
    {
        return body;
    }

}
