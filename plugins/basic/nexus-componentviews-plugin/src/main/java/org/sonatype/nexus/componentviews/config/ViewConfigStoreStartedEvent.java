package org.sonatype.nexus.componentviews.config;

/**
 * This is an event chiefly designed to facilitate example view plugins, so they can register configurations.  (There is
 * currently no way to do this in the absence of a view config UI.)
 *
 * @since 3.0
 */
public class ViewConfigStoreStartedEvent
    extends ViewConfigStoreEvent
{
  public ViewConfigStoreStartedEvent(final ViewConfigStore component) {
    super(component);
  }
}
