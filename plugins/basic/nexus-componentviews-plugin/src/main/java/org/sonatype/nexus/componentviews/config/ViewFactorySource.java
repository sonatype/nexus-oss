package org.sonatype.nexus.componentviews.config;

import javax.annotation.Nullable;

/**
 * A simple provider of {@link ViewFactory view factories}.
 *
 * @since 3.0
 */
public interface ViewFactorySource
{
  @Nullable
  ViewFactory getRecipe(String recipeName);
}
