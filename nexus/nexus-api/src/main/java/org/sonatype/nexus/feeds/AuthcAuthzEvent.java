package org.sonatype.nexus.feeds;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A event that encapsulate authentication and authorization event.
 * 
 * @author juven
 */
public class AuthcAuthzEvent
{

    /**
     * The date of event.
     */
    private Date eventDate;

    private final String action;

    private final String message;

    /**
     * The context of event.
     */
    private final Map<String, Object> eventContext;

    public AuthcAuthzEvent( String action, String message )
    {
        this.action = action;

        this.message = message;

        this.eventDate = new Date();

        this.eventContext = new HashMap<String, Object>();
    }

    public Date getEventDate()
    {
        return eventDate;
    }

    public String getMessage()
    {
        return message;
    }

    public Map<String, Object> getEventContext()
    {
        return eventContext;
    }

    public String getAction()
    {
        return action;
    }


    public void setEventDate( Date eventDate )
    {
        this.eventDate = eventDate;
    }

}
