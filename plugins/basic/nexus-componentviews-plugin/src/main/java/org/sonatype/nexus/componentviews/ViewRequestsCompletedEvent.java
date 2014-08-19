package org.sonatype.nexus.componentviews;

/**
 * Fired after a view has been unregistered from a {@link ViewRegistry}, and any in-process requests have completed,
 * making it safe for the view's resources to be dumped.
 *
 * @since 3.0
 */
public class ViewRequestsCompletedEvent
    extends ViewRegistryEvent
{
  public ViewRequestsCompletedEvent(final ViewRegistry component, final View view) {
    super(component, view);
  }
}
