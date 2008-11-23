package org.sonatype.nexus.plugins.rest;

import java.util.List;
import java.util.Map;

public class AbstractNexusResourceBundle
    implements NexusResourceBundle
{
    public List<StaticResource> getContributedResouces()
    {
        return null;
    }

    public String getPreHeadContribution( Map<String, Object> context )
    {
        return null;
    }

    public String getPostHeadContribution( Map<String, Object> context )
    {
        return null;
    }

    public String getPreBodyContribution( Map<String, Object> context )
    {
        return null;
    }

    public String getPostBodyContribution( Map<String, Object> context )
    {
        return null;
    }

}
