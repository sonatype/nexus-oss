package org.sonatype.nexus.templates;

import org.sonatype.nexus.configuration.CoreConfiguration;

public interface ConfigurableTemplate
    extends Template
{
    /**
     * Returns the core configuration that this template holds.
     * 
     * @return
     */
    CoreConfiguration getCoreConfiguration();
}
