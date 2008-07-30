package org.sonatype.nexus.security.filter;

import javax.servlet.ServletRequest;

public class RepositoryTargetJSecurityFilterConfig
    extends NexusFilterConfiguration
{
    protected String getPathWithinApplication( ServletRequest request )
    {
        String appPath = super.getPathWithinApplication( request );

        // get the Target for this path and return that one instead

        return appPath;
    }
}
