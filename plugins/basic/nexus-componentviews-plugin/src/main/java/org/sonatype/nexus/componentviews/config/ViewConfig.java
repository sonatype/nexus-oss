package org.sonatype.nexus.componentviews.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Immutable parameters for a {@link ViewFactory}, supplied by an end-user administrator.
 *
 * @since 3.0
 */
public class ViewConfig
{
  private final String viewName;

  private final String recipeName;

  private final Map<String, Object> configuration;

  public ViewConfig(final String viewName, final String recipeName,
                    final Map<String, Object> configuration)
  {
    this.viewName = checkNotNull(viewName);
    this.recipeName = checkNotNull(recipeName);
    this.configuration = Collections.unmodifiableMap(new HashMap<>(checkNotNull(configuration)));
  }

  public String getViewName() {
    return viewName;
  }

  public String getRecipeName() {
    return recipeName;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }
}
