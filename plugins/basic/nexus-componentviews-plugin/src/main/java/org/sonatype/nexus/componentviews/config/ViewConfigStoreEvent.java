package org.sonatype.nexus.componentviews.config;

import org.sonatype.nexus.events.AbstractEvent;

/**
 * @since 3.0
 */
public abstract class ViewConfigStoreEvent
    extends AbstractEvent<ViewConfigStore>
{
  public ViewConfigStoreEvent(final ViewConfigStore component) {
    super(component);
  }
}
