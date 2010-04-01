package org.sonatype.nexus.notification;

public interface NotificationManager
{
    boolean isEnabled();

    void setEnabled( boolean val );

    // TODO: implement full CRUD when redoing
    // void createNotificationTarget( NotificationTarget target );

    NotificationTarget readNotificationTarget( String targetId );

    void updateNotificationTarget( NotificationTarget target );

    // TODO: implement full CRUD when redoing
    // void deleteNotificationTarget( String targetId );

    // --

    void notifyTargets( NotificationRequest request );
}
