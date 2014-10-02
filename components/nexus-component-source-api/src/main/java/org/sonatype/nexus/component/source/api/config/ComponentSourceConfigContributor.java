package org.sonatype.nexus.component.source.api.config;

import javax.inject.Named;

/**
 * In the absence of a UI to define example sources, plugins can provide configuration at nexus start time by
 * {@link Named naming} instances of this class.
 *
 * @since 3.0
 */
public interface ComponentSourceConfigContributor
{
  void contributeTo(ComponentSourceConfigStore store);
}
