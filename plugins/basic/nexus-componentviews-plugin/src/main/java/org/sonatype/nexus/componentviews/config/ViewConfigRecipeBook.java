package org.sonatype.nexus.componentviews.config;

/**
 * A simple provider of recipes.
 *
 * @since 3.0
 */
public interface ViewConfigRecipeBook
{
  ViewConfigRecipe getRecipe(String recipeName);
}
