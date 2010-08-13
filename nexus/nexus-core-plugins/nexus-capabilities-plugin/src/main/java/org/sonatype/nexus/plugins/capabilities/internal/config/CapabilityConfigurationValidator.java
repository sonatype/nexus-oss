package org.sonatype.nexus.plugins.capabilities.internal.config;

import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.plugin.Managed;

@Managed
public interface CapabilityConfigurationValidator
    extends ConfigurationValidator
{

    ValidationResponse validate( CCapability capability, boolean isCreateMode );

}
