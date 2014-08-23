package org.sonatype.nexus.componentviews.internal;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewConfigRecipe;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipeBook;

import org.eclipse.sisu.EagerSingleton;

/**
 * @since 3.0
 */
@Named
@Singleton
public class RecipeBook
    implements ViewConfigRecipeBook
{
  private final Set<ViewConfigRecipe> recipes;

  @Inject
  public RecipeBook(Set<ViewConfigRecipe> recipes) {
    this.recipes = recipes;
  }

  @Override
  public ViewConfigRecipe getRecipe(final String recipeName) {
    for (ViewConfigRecipe recipe : recipes) {
      if (recipe.getName().equals(recipeName)) {
        return recipe;
      }
    }
    return null;
  }
}
