package org.sonatype.nexus.notification;

import java.util.HashSet;
import java.util.Set;

public class NotificationRequest
{
    public static final NotificationRequest EMPTY = new NotificationRequest( NotificationMessage.EMPTY_MESSAGE );

    private final Set<NotificationTarget> targets;

    private final NotificationMessage message;

    public NotificationRequest( NotificationMessage message )
    {
        this( message, new HashSet<NotificationTarget>() );
    }

    public NotificationRequest( NotificationMessage message, Set<NotificationTarget> targets )
    {
        this.message = message;

        this.targets = targets;
    }

    public Set<NotificationTarget> getTargets()
    {
        return targets;
    }

    public NotificationMessage getMessage()
    {
        return message;
    }

    public boolean isEmpty()
    {
        return getTargets().isEmpty();
    }
}
