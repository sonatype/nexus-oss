package org.sonatype.nexus.error.reporting;

import java.util.HashMap;
import java.util.Map;

public class ErrorReportRequest
{
    private String title;
    
    private String description;
    
    private Throwable throwable;
    
    private Map<String,Object> context = new HashMap<String,Object>();
    
    public Throwable getThrowable()
    {
        return throwable;
    }
    
    public void setThrowable( Throwable throwable )
    {
        this.throwable = throwable;
    }
    
    public Map<String, Object> getContext()
    {
        return context;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setDescription( String description )
    {
        this.description = description;
    }
    
    /**
     * if a title is set, the throwable will not be used, as manual
     * submission is assumed
     * 
     * @param title
     */
    public void setTitle( String title )
    {
        this.title = title;
    }
}
