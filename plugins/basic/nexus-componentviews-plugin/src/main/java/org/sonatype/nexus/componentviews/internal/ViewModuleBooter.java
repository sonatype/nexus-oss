package org.sonatype.nexus.componentviews.internal;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigContributor;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.componentviews.config.ViewFactory;
import org.sonatype.nexus.componentviews.config.ViewFactorySource;
import org.sonatype.nexus.componentviews.example.PathListViewFactory;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ensures that all defined views are created as Nexus starts.
 *
 * @since 3.0
 */
@Named
@EagerSingleton
public class ViewModuleBooter
    extends ComponentSupport
    implements EventSubscriber
{
  private final EventBus eventBus;

  private final ViewFactorySource recipeBook;

  private final ViewConfigStore viewConfigStore;

  private final ViewRegistry viewRegistry;

  private final Set<ViewConfigContributor> configContributors;

  // TODO: This is a temporary example view
  private final PathListViewFactory pathListViewFactory;

  @Inject
  public ViewModuleBooter(final EventBus eventBus, final ViewFactorySource recipeBook,
                          final ViewConfigStore viewConfigStore, final ViewRegistry viewRegistry,
                          final PathListViewFactory pathListViewFactory,
                          final Set<ViewConfigContributor> configContributors, final Set<ViewFactory> recipes)
  {
    this.configContributors = checkNotNull(configContributors);
    this.eventBus = checkNotNull(eventBus);
    this.recipeBook = checkNotNull(recipeBook);
    this.viewConfigStore = checkNotNull(viewConfigStore);
    this.viewRegistry = checkNotNull(viewRegistry);
    this.pathListViewFactory = checkNotNull(pathListViewFactory);
  }

  @Subscribe
  public void on(NexusInitializedEvent event) throws Exception {
    viewConfigStore.start();
    for (ViewConfigContributor contrib : configContributors) {
      contrib.contributeTo(viewConfigStore);
    }
    createViews();
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    viewConfigStore.stop();
  }

  private void createViews()
      throws IOException
  {
    log.debug("Creating component views.");
    for (ViewConfig config : viewConfigStore.getAll().values()) {
      final ViewFactory recipe = recipeBook.getRecipe(config.getRecipeName());
      if (recipe != null) {
        log.debug("Creating view {}" + config.getViewName());
        viewRegistry.registerView(recipe.createView(config));
      }
      else {
        log.error("No view creation found for user supplied config {}", config);
        throw new IllegalStateException("No recipe found with recipe name " + config.getRecipeName());
      }
    }
  }
}

