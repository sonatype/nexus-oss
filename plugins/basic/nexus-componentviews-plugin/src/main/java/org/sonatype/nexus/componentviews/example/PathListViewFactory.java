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
import org.sonatype.nexus.componentviews.config.ViewFactory;

import com.google.common.collect.ImmutableMap;

/**
 * @since 3.0
 */
@Named
@Singleton
public class PathListViewFactory
    implements ViewFactory
{
  private final NotFoundHandler notFound;

  @Inject
  public PathListViewFactory(final NotFoundHandler notFound) {
    this.notFound = notFound;
  }

  @Override
  public String getFactoryName() {
    return "pathList";
  }

  @Override
  public View createView(final ViewConfig config) {
    final Router router = new Router(Arrays.asList((RequestMatcher) new AllRequestMatcher("pathList")));

    final View view = new View(config.getViewName(), router,
        ImmutableMap.of("pathList", (Handler) new ExamplePathListHandler()), new NotFoundHandler(), config);

    return view;
  }
}
