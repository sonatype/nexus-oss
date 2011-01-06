package org.sonatype.nexus.feeds;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.RequestContext;

public class AbstractEvent
{
    /**
     * The date of event.
     */
    private final Date eventDate;

    /**
     * The action.
     */
    private final String action;

    /**
     * Human message/descritpion.
     */
    private final String message;

    /**
     * The context of event.
     */
    private final Map<String, Object> eventContext;

    public AbstractEvent( final Date eventDate, final String action, final String message )
    {
        this.eventDate = eventDate;

        this.action = action;

        this.message = message;

        this.eventContext = new HashMap<String, Object>();
    }

    public Date getEventDate()
    {
        return eventDate;
    }

    public String getAction()
    {
        return action;
    }

    public String getMessage()
    {
        return message;
    }

    public Map<String, Object> getEventContext()
    {
        return eventContext;
    }

    public void addEventContext( Map<String, ?> ctx )
    {
        if ( ctx instanceof RequestContext )
        {
            getEventContext().putAll( ( (RequestContext) ctx ).flatten() );
        }
        else
        {
            getEventContext().putAll( ctx );
        }
    }

    public String toString()
    {
        return getMessage();
    }
}
