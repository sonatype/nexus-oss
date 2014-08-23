package org.sonatype.nexus.componentviews.config;

import javax.annotation.Nullable;

/**
 * A simple provider of recipes.
 *
 * @since 3.0
 */
public interface ViewConfigRecipeBook
{
  @Nullable
  ViewConfigRecipe getRecipe(String recipeName);
}
