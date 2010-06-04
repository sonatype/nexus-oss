package org.sonatype.nexus.notification;

/**
 * This class will be removed, when we implement real notification mechanism. Right now, this only concentrates all the
 * "cheating" elements to implement the simplest notification we need for now. When doing right, 1st step is to _remove_
 * this class, and all the points needing some work will be "highlighted" by compiler ;)
 * 
 * @author cstamas
 */
public class NotificationCheat
{
    public static final String AUTO_BLOCK_NOTIFICATION_GROUP_ID = "autoBlockTarget";

    protected static final String CARRIER_KEY = EmailCarrier.KEY;
}
