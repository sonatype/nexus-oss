package org.sonatype.nexus.componentviews;

/**
 * Fired when a {@link View} is removed from the {@link ViewRegistry} and will therefore no longer be given any
 * new requests, although the View may still be handling requests that were dispatched to it before it was
 * unregistered.
 *
 * @since 3.0
 */
public class ViewUnregisteredEvent
    extends ViewRegistryEvent
{
  public ViewUnregisteredEvent(final ViewRegistry component, final View view) {
    super(component, view);
  }
}
