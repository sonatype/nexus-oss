package org.sonatype.nexus.componentviews.internal;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewFactory;
import org.sonatype.nexus.componentviews.config.ViewFactorySource;

/**
 * A simple, in-memory implementation of {@link ViewFactorySource} that collects all {@link ViewFactory} implementations
 * exposed through Guice.
 *
 * @since 3.0
 */
@Named
@Singleton
public class InMemoryViewFactorySource
    implements ViewFactorySource
{
  private final Set<ViewFactory> recipes;

  @Inject
  public InMemoryViewFactorySource(final Set<ViewFactory> recipes) {
    this.recipes = recipes;
  }

  @Override
  public ViewFactory getRecipe(final String recipeName) {
    for (ViewFactory recipe : recipes) {
      if (recipe.getFactoryName().equals(recipeName)) {
        return recipe;
      }
    }
    return null;
  }
}
