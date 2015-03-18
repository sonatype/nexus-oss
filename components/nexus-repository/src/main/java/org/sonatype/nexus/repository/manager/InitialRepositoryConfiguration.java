package org.sonatype.nexus.repository.manager;

import java.util.List;

import org.sonatype.nexus.repository.config.Configuration;

/**
 * @since 3.0
 */
public interface InitialRepositoryConfiguration
{
  /**
   * Provides Configurations that should be be initially provisioned.
   */
  List<Configuration> getRepositoryConfigurations();
}
