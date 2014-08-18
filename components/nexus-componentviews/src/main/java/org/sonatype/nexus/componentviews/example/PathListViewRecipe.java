package org.sonatype.nexus.componentviews.example;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.AlwaysMatchesRoute;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.Route;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.UserSuppliedViewConfig;
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
  public View createView(final UserSuppliedViewConfig userSuppliedConfig) {
    final Router router = new Router(Arrays.asList((Route) new AlwaysMatchesRoute("pathList")));

    final View view = new View(userSuppliedConfig.getViewName(),
        router,
        ImmutableMap.of("pathList", (Handler) new ExamplePathListHandler()),
        new NotFoundHandler());

    return view;
  }
}
