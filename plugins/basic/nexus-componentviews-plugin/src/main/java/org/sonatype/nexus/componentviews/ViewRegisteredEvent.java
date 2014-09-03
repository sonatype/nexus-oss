package org.sonatype.nexus.componentviews;

/**
 * Fired when the {@link ViewRegistry} registers a new {@link View}.
 * @since 3.0
 */
public class ViewRegisteredEvent
    extends ViewRegistryEvent
{
  public ViewRegisteredEvent(final ViewRegistry component, final View view) {
    super(component, view);
  }
}
