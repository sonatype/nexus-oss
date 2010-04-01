package org.sonatype.nexus.notification;

/**
 * The message to send out using notification carriers. This might be sourced from a simple string, or template, but it
 * has to be reusable (and cached if needed), since in case of multiple targets, the same message instance is reused!
 * TODO: rethink this. We need more subtle abstraction to support multiple message formats and/or sources. This will do
 * for now.
 * 
 * @author cstamas
 */
public interface NotificationMessage
{
    String getMessageTitle();

    String getMessageBody();

    // --

    public static final NotificationMessage EMPTY_MESSAGE = new NotificationMessage()
    {
        public String getMessageTitle()
        {
            return "";
        }

        public String getMessageBody()
        {
            return "";
        }
    };
}
