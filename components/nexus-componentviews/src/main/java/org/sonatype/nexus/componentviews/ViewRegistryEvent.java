package org.sonatype.nexus.componentviews;

import org.sonatype.nexus.events.AbstractEvent;

/**
 * @since 3.0
 */
public abstract class ViewRegistryEvent
    extends AbstractEvent<ViewRegistry>
{
  private final View view;

  public ViewRegistryEvent(final ViewRegistry component, final View view) {
    super(component);
    this.view = view;
  }
}
