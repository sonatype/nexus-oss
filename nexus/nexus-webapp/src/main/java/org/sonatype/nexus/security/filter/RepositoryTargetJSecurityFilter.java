package org.sonatype.nexus.security.filter;

import org.sonatype.plexus.jsecurity.web.filter.PlexusJSecurityFilter;

public class RepositoryTargetJSecurityFilter
    extends PlexusJSecurityFilter
{
    public RepositoryTargetJSecurityFilter()
    {
        super();

        this.configClassName = RepositoryTargetJSecurityFilterConfig.class.getName();
    }

}
