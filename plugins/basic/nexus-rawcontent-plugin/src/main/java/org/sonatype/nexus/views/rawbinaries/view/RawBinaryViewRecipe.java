package org.sonatype.nexus.views.rawbinaries.view;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.componentviews.AllRequestMatcher;
import org.sonatype.nexus.componentviews.Handler;
import org.sonatype.nexus.componentviews.RequestMatcher;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipe;
import org.sonatype.nexus.componentviews.example.NotFoundHandler;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;

import com.google.common.collect.ImmutableMap;
import org.eclipse.sisu.EagerSingleton;

import static java.util.Arrays.asList;

/**
 * @since 3.0
 */
public class RawBinaryViewRecipe
    implements ViewConfigRecipe
{
  public static final String RAW_BINARY_RECIPE = "rawBinary";

  private final RawBinaryStore binaryStore;

  @Inject
  public RawBinaryViewRecipe(final RawBinaryStore binaryStore) {
    this.binaryStore = binaryStore;
  }

  @Override
  public String getName() {
    return RAW_BINARY_RECIPE;
  }

  @Override
  public View createView(final ViewConfig userSuppliedConfig) {
    final AllRequestMatcher binariesRequestMatcher = new AllRequestMatcher("binaries");

    final Router router = new Router(asList((RequestMatcher) binariesRequestMatcher));

    final ImmutableMap<String, Handler> handlers = ImmutableMap
        .of("binaries", (Handler) new RawBinariesHandler(binaryStore));

    return new View(userSuppliedConfig.getViewName(), router, handlers,
        new NotFoundHandler());
  }
}
