package org.sonatype.nexus.rest.schedules;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;

public class AbstractScheduledServiceResourceHandler
    extends AbstractNexusResourceHandler
{
    /** Schedule Type Off.*/
    public static final String SCHEDULE_TYPE_NONE = "none";
    
    /** Schedule Type Once.*/
    public static final String SCHEDULE_TYPE_ONCE = "once";
    
    /** Schedule Type Daily. */
    public static final String SCHEDULE_TYPE_DAILY = "daily";
    
    /** Schedule Type Weekly. */
    public static final String SCHEDULE_TYPE_WEEKLY = "weekly";
    
    /** Schedule Type Monthly. */
    public static final String SCHEDULE_TYPE_MONTHLY = "monthly";
    
    /** Schedule Type Advanced. */
    public static final String SCHEDULE_TYPE_ADVANCED = "advanced";
    /**
     * Standard constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractScheduledServiceResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }
}
