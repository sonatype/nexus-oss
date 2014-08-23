package com.sonatype.nexus.componentviews;

import java.io.IOException;

import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipe;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipeBook;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;

import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @since 3.0
 */
public class ViewConfigApiDemo
{
  /**
   * Render all recipes in the config store, such as might be done as Nexus starts.
   */
  @Test
  public void instantiateAndRegisterAllViews() throws IOException {
    ViewConfigStore usvcStore = mock(ViewConfigStore.class);
    ViewConfigRecipeBook recipeBook = mock(ViewConfigRecipeBook.class);
    ViewRegistry viewRegistry = mock(ViewRegistry.class);

    for (ViewConfig config : usvcStore.getAll().values()) {
      final ViewConfigRecipe recipe = recipeBook.getRecipe(config.getRecipeName());
      final View view = recipe.createView(config);
      viewRegistry.registerView(view);
    }
  }

  /**
   * Reloading a view in response to a configuration change.
   */
  public void reloadView() {
    final ViewConfigRecipeBook recipeBook = mock(ViewConfigRecipeBook.class);
    final ViewRegistry viewRegistry = mock(ViewRegistry.class);

    // A changed (and persisted) configuration created by an admin
    final ViewConfig config = mock(ViewConfig.class);

    final ViewConfigRecipe recipe = recipeBook.getRecipe(config.getRecipeName());
    final View newView = recipe.createView(config);

    // If there's an existing view, we need to wait until it's safely unregistered.
    final View existingView = viewRegistry.findViewByName(config.getViewName());
    if (existingView != null) {
      viewRegistry.unregisterView(existingView);
    }
    viewRegistry.registerView(newView);
  }
}
