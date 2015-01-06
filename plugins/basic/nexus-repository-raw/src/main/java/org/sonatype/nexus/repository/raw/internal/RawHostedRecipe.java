package org.sonatype.nexus.repository.raw.internal;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.RecipeSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.httpbridge.HttpHandlers;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named("raw-hosted")
@Singleton
public class RawHostedRecipe
    extends RecipeSupport
{
  private final Provider<RawStorageFacetImpl> storageFacet;

  private final Provider<ConfigurableViewFacet> viewFacet;

  private final TimingHandler timingHandler;

  private final RawStorageHandler rawStorageHandler;

  @Inject
  public RawHostedRecipe(final Type type, final Format format,
                         final Provider<ConfigurableViewFacet> viewFacet,
                         final Provider<RawStorageFacetImpl> storageFacet,
                         final TimingHandler timingHandler,
                         final RawStorageHandler rawStorageHandler)
  {
    super(type, format);
    this.viewFacet = checkNotNull(viewFacet);
    this.storageFacet = checkNotNull(storageFacet);
    this.timingHandler = checkNotNull(timingHandler);
    this.rawStorageHandler = checkNotNull(rawStorageHandler);
  }

  @Override
  public void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(storageFacet.get());
    repository.attach(configure(viewFacet.get()));
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder();

    //builder.route(new Route.Builder()
    //    .matcher(or(
    //        new LiteralMatcher("/"),
    //        new LiteralMatcher("/index.html")))
    //    .handler(timingHandler)
    //    .handler(indexHtmlHandler)
    //    .create());

    builder.route(new Route.Builder()
        .matcher(new TokenMatcher("/{name:.+}"))
        .handler(timingHandler)
        .handler(rawStorageHandler)
        .create());

    builder.defaultHandlers(
        HttpHandlers.badRequest()
    );

    facet.configure(builder.create());

    return facet;
  }
}
