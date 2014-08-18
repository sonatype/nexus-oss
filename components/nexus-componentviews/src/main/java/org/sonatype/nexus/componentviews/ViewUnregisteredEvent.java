package org.sonatype.nexus.componentviews;

/**
 * @since 3.0
 */
public class ViewUnregisteredEvent
    extends ViewRegistryEvent
{
  public ViewUnregisteredEvent(final ViewRegistry component, final View view) {
    super(component, view);
  }
}
