package org.sonatype.nexus.security.filter;

import javax.servlet.ServletRequest;

import org.sonatype.plexus.jsecurity.web.filter.PlexusConfiguration;

public class RepositoryTargetJSecurityFilterConfig
    extends PlexusConfiguration
{
    protected String getPathWithinApplication( ServletRequest request )
    {
        String appPath = super.getPathWithinApplication( request );

        // get the Target for this path and return that one instead

        return appPath;
    }
}
