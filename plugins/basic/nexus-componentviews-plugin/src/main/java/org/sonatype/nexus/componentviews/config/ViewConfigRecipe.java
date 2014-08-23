package org.sonatype.nexus.componentviews.config;

import org.sonatype.nexus.componentviews.View;

/**
 * A process for taking a view's user-supplied config (USC) and rendering it into a usable {@link View}.
 *
 * @since 3.0
 */
public interface ViewConfigRecipe
{
  String getName();

  View createView(ViewConfig userSuppliedConfig);
}
