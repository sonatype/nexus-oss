package org.sonatype.nexus.security.filter;

public class RepositoryTargetJSecurityFilter
    extends NexusJSecurityFilter
{
    public RepositoryTargetJSecurityFilter()
    {
        super();

        this.configClassName = RepositoryTargetJSecurityFilterConfig.class.getName();
    }

}
