package org.sonatype.nexus.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * {@link SystemStatus} provider.
 *
 * @since 3.0
 */
@Named
public class SystemStatusProvider
  extends ComponentSupport
  implements Provider<SystemStatus>
{
  private final ApplicationStatusSource statusSource;

  @Inject
  public SystemStatusProvider(final ApplicationStatusSource statusSource) {
    this.statusSource = statusSource;
  }

  @Override
  public SystemStatus get() {
    return statusSource.getSystemStatus();
  }
}
