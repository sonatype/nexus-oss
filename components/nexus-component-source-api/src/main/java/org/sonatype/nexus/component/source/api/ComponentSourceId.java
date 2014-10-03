package org.sonatype.nexus.component.source.api;

import org.sonatype.nexus.component.model.ComponentOriginId;

/**
 * A unique identifier for component sources.
 *
 * @since 3.0
 */
public class ComponentSourceId
    extends ComponentOriginId
{
  public ComponentSourceId(final String name, final String identifier) {
    super(name, identifier);
  }

  public ComponentSourceId(final String uniqueString) {
    super(uniqueString);
  }
}
