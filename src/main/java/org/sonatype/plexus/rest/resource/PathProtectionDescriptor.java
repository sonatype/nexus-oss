package org.sonatype.plexus.rest.resource;

public class PathProtectionDescriptor
{
    private String pathPattern;

    private String filterExpression;

    public PathProtectionDescriptor( String pathPattern, String filterExpression )
    {
        this.pathPattern = pathPattern;

        this.filterExpression = filterExpression;
    }

    public String getPathPattern()
    {
        return pathPattern;
    }

    public void setPathPattern( String pathPattern )
    {
        this.pathPattern = pathPattern;
    }

    public String getFilterExpression()
    {
        return filterExpression;
    }

    public void setFilterExpression( String filterExpression )
    {
        this.filterExpression = filterExpression;
    }

}
