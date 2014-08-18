package org.sonatype.nexus.componentviews;

/**
 * @since 3.0
 */
public class ViewRegisteredEvent
    extends ViewRegistryEvent
{
  public ViewRegisteredEvent(final ViewRegistry component, final View view) {
    super(component, view);
  }
}
