package org.sonatype.nexus.error.reporting;

import java.util.HashMap;
import java.util.Map;

public class ErrorReportRequest
{
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
}
