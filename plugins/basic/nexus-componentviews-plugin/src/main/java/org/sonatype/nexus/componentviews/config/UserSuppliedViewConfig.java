package org.sonatype.nexus.componentviews.config;

import java.util.Map;

/**
 * Parameters for a {@link ViewConfigRecipe}, supplied by an end-user administrator.
 *
 * @since 3.0
 */
public interface UserSuppliedViewConfig
{
  /**
   * What's the name of the view this will ultimately create?
   */
  String getViewName();

  /**
   * What recipe should this config be combined with?
   */
  String getRecipeName();

  /**
   * Parameters for the recipe.
   */
  Map<String, Object> getConfiguration();
}
