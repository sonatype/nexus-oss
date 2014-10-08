package org.sonatype.nexus.component.source.api.config;

import org.sonatype.nexus.component.source.api.ComponentSourceRegistry;
import org.sonatype.nexus.events.AbstractEvent;

/**
 * Fired when the {@link ComponentSourceRegistry} has been populated as Nexus starts.
 * @since 3.0
 */
public class ComponentSourceRegistryInitializedEvent
    extends AbstractEvent<ComponentSourceRegistry>
{
  public ComponentSourceRegistryInitializedEvent(
      final ComponentSourceRegistry component)
  {
    super(component);
  }
}
