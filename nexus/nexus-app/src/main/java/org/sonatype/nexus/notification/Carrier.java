package org.sonatype.nexus.notification;

public interface Carrier
{
    void notifyTarget( NotificationTarget target, NotificationMessage message )
        throws NotificationException;
}
