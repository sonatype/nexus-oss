package org.sonatype.nexus.notification.events;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.sonatype.nexus.notification.NotificationMessage;
import org.sonatype.nexus.proxy.events.RepositoryEventProxyModeChanged;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.security.usermanagement.User;

public class RepositoryEventProxyModeChangedMessage
    implements NotificationMessage
{
    private final RepositoryEventProxyModeChanged repositoryEventProxyModeChanged;

    private final User user;

    private final String title;

    private final String body;

    public RepositoryEventProxyModeChangedMessage( RepositoryEventProxyModeChanged revt, User user )
    {
        this.repositoryEventProxyModeChanged = revt;

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

        sb.append( "\" (repoId=" ).append( revt.getRepository().getId() ).append( ") was set to " );

        if ( ProxyMode.ALLOW.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Allow." );
        }
        else if ( ProxyMode.BLOCKED_AUTO.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Blocked (automatically by Nexus)." );
        }
        else if ( ProxyMode.BLOCKED_MANUAL.equals( revt.getNewProxyMode() ) )
        {
            sb.append( "Blocked (by a user)." );
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

    public RepositoryEventProxyModeChanged getRepositoryEventProxyModeChanged()
    {
        return repositoryEventProxyModeChanged;
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
