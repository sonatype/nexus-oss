package org.sonatype.nexus.views.rawbinaries;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigRecipeBook;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.componentviews.config.ViewConfigStoreStartedEvent;
import org.sonatype.nexus.componentviews.example.PathListViewRecipe;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;

import com.google.common.eventbus.Subscribe;
import org.eclipse.sisu.EagerSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ensures that there's a view config defined to
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class RawBinariesViewBooter
    implements EventSubscriber
{
  private static final Logger log = LoggerFactory.getLogger(RawBinariesViewBooter.class);

  private final ViewConfigRecipeBook recipeBook;

  private final ViewConfigStore viewConfigStore;

  private final ViewRegistry viewRegistry;

  // TODO: This is a temporary example view
  private final PathListViewRecipe pathListViewRecipe;

  private final RawBinaryStore rawBinaryStore;

  private static final String VIEW_NAME = "binaries";

  @Inject
  public RawBinariesViewBooter(final ViewConfigRecipeBook recipeBook,
                               final ViewConfigStore viewConfigStore, final ViewRegistry viewRegistry,
                               final PathListViewRecipe pathListViewRecipe, final RawBinaryStore rawBinaryStore)
  {
    this.rawBinaryStore = checkNotNull(rawBinaryStore);
    this.recipeBook = checkNotNull(recipeBook);
    this.viewConfigStore = checkNotNull(viewConfigStore);
    this.viewRegistry = checkNotNull(viewRegistry);
    this.pathListViewRecipe = checkNotNull(pathListViewRecipe);
  }

  @Subscribe
  public void on(NexusInitializedEvent event) throws Exception {
    rawBinaryStore.start();
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    rawBinaryStore.stop();
  }

  @Subscribe
  public void on(NexusStartedEvent event) throws IOException {
    final ViewConfig viewConfig = viewConfigStore.get(VIEW_NAME);
    if (viewConfig == null) {
      log.info("Force-adding config for /views/{}.", VIEW_NAME);
      viewConfigStore.add(
          new ViewConfig(VIEW_NAME, RawBinaryViewRecipe.RAW_BINARY_RECIPE, Collections.<String, Object>emptyMap()));
    }
  }
}

