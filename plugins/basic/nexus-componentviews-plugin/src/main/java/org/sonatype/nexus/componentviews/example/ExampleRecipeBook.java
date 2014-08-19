package org.sonatype.nexus.componentviews.example;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewConfigRecipe;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipeBook;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named
@Singleton
public class ExampleRecipeBook
    implements ViewConfigRecipeBook
{
  private final PathListViewRecipe pathListViewRecipe;

  @Inject
  public ExampleRecipeBook(final PathListViewRecipe pathListViewRecipe) {
    this.pathListViewRecipe = checkNotNull(pathListViewRecipe);
  }

  @Override
  public ViewConfigRecipe getRecipe(final String recipeName) {
    if ("pathListRecipe".equals(recipeName)) {
      return pathListViewRecipe;
    }
    return null;
  }
}
