package org.sonatype.nexus.componentviews.example;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.AllRequestMatcher;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.RequestMatcher;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipe;

import com.google.common.collect.ImmutableMap;

/**
 * @since 3.0
 */
@Named
@Singleton
public class PathListViewRecipe
    implements ViewConfigRecipe
{
  private final NotFoundHandler notFound;

  @Inject
  public PathListViewRecipe(final NotFoundHandler notFound) {
    this.notFound = notFound;
  }

  @Override
  public String getName() {
    return "pathList";
  }

  @Override
  public View createView(final ViewConfig userSuppliedConfig) {
    final Router router = new Router(Arrays.asList((RequestMatcher) new AllRequestMatcher("pathList")));

    final View view = new View(userSuppliedConfig.getViewName(),
        router,
        ImmutableMap.of("pathList", (Handler) new ExamplePathListHandler()),
        new NotFoundHandler());

    return view;
  }
}
