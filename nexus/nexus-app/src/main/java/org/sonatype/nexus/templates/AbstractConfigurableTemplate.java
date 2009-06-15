package org.sonatype.nexus.templates;

import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;

public abstract class AbstractConfigurableTemplate<I>
    extends AbstractTemplate<I>
    implements ConfigurableTemplate<I>
{
    private final CoreConfiguration coreConfiguration;

    public AbstractConfigurableTemplate( String id, String description, CoreConfiguration coreConfiguration )
    {
        super( id, description );

        this.coreConfiguration = coreConfiguration;
    }

    public CoreConfiguration getCoreConfiguration()
    {
        return coreConfiguration;
    }

    public ExternalConfiguration getExternalConfiguration()
    {
        return getCoreConfiguration().getExternalConfiguration();
    }
}