package org.sonatype.nexus.templates;

import org.sonatype.nexus.configuration.CoreConfiguration;

public interface ConfigurableTemplate<I>
    extends Template<I>
{
    /**
     * Returns the core configuration that this template holds.
     * 
     * @return
     */
    CoreConfiguration getCoreConfiguration();
}
