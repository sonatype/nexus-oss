package org.sonatype.nexus.views.rawbinaries;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.AllRequestMatcher;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.RequestMatcher;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipe;
import org.sonatype.nexus.componentviews.example.NotFoundHandler;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;

/**
 * @since 3.0
 */
@Named
@Singleton
public class RawBinaryViewRecipe
    implements ViewConfigRecipe
{
  public static final String RAW_BINARY_RECIPE = "rawBinary";

  @Override
  public String getName() {
    return RAW_BINARY_RECIPE;
  }

  @Override
  public View createView(final ViewConfig userSuppliedConfig) {
    final AllRequestMatcher binariesRequestMatcher = new AllRequestMatcher("binaries");

    final Router router = new Router(asList((RequestMatcher) binariesRequestMatcher));

    final ImmutableMap<String, Handler> handlers = ImmutableMap.of("binaries", (Handler) null);

    return new View(userSuppliedConfig.getViewName(), router, handlers,
        new NotFoundHandler());
  }
}
