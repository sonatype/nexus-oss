package org.sonatype.nexus.views.rawbinaries.view;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigContributor;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Contributes configuration for a "binaries" view.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RawBinaryConfigContribution
    extends ComponentSupport
    implements ViewConfigContributor
{
  private static final String VIEW_NAME = "binaries";

  @Override
  public void contributeTo(final ViewConfigStore viewConfigStore) throws IOException {
    final ViewConfig viewConfig = viewConfigStore.get(VIEW_NAME);
    if (viewConfig == null) {
      log.info("Force-adding config for /views/{}.", VIEW_NAME);
      viewConfigStore.add(
          new ViewConfig(VIEW_NAME, RawBinaryViewRecipe.RAW_BINARY_RECIPE, Collections.<String, Object>emptyMap()));
    }
  }
}
