package org.sonatype.nexus.error.reporting;

public class ErrorReportResponse
{
    private String jiraUrl;
    private boolean success;
    private boolean created;
    
    public String getJiraUrl()
    {
        return jiraUrl;
    }
    
    public void setJiraUrl( String jiraUrl )
    {
        this.jiraUrl = jiraUrl;
    }
    
    public boolean isCreated()
    {
        return created;
    }
    
    public void setCreated( boolean created )
    {
        this.created = created;
    }
    
    public boolean isSuccess()
    {
        return success;
    }
    
    public void setSuccess( boolean success )
    {
        this.success = success;
    }
}
