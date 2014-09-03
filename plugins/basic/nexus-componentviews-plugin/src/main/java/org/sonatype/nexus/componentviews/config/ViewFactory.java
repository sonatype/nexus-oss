package org.sonatype.nexus.componentviews.config;

import org.sonatype.nexus.componentviews.View;

/**
 * A process for taking a view's user-supplied config (USC) and rendering it into a usable {@link View}.
 *
 * @since 3.0
 */
public interface ViewFactory
{
  /**
   * This is an identifier used to look up the factory. (It's not the name of the view the factory produces, that comes
   * from the {@link ViewConfig}.
   */
  String getFactoryName();

  /**
   * Construct a view, ready to respond to incoming requests.
   */
  View createView(ViewConfig userSuppliedConfig);
}
