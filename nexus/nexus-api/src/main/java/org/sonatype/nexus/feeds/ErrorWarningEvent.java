package org.sonatype.nexus.feeds;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that encapsulates all nexus errors and warnings
 * 
 * @author juven
 */
public class ErrorWarningEvent
{
    public static final String ACTION_ERROR = "error";
    
    public static final String ACTION_WARNING = "warning";
    
    private Date eventDate;

    private final String action;

    private final String message;

    private final String stackTrace;

    private final Map<String, Object> eventContext;

    public ErrorWarningEvent( String action, String message )
    {
        this( action, message, "" );
    }

    public ErrorWarningEvent( String action, String message, String stackTrace )
    {
        this.action = action;

        this.message = message;

        this.eventDate = new Date();

        this.eventContext = new HashMap<String, Object>();

        this.stackTrace = stackTrace;
    }

    public Date getEventDate()
    {
        return eventDate;
    }

    public void setEventDate( Date date )
    {
        this.eventDate = date;
    }

    public Map<String, Object> getEventContext()
    {
        return eventContext;
    }

    public String getAction()
    {
        return action;
    }

    public String getMessage()
    {
        return message;
    }

    public String getStackTrace()
    {
        return stackTrace;
    }

    public String toString()
    {
        return getMessage();
    }
}
