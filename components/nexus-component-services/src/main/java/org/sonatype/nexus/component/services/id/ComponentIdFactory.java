package org.sonatype.nexus.component.services.id;

import org.sonatype.nexus.component.model.ComponentId;

/**
 * A factory for creating new {@link ComponentId}s, necessary for new components.
 *
 * @since 3.0
 */
public interface ComponentIdFactory
{
  /**
   * Create a new {@link ComponentId} for a new component.
   */
  ComponentId newId();

  /**
   * Restore a {@link ComponentId} from its "unique string" format, as is necessary
   */
  ComponentId fromUniqueString(String uniqueString);
}
