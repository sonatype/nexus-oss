package org.sonatype.nexus.componentviews.example;

import java.io.IOException;
import java.util.HashMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigContributor;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * A temporary class that contributes the pathList view, in the absence of a UI to do it.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ExampleConfigContributor
    extends ComponentSupport
    implements ViewConfigContributor
{
  @Override
  public void contributeTo(final ViewConfigStore store) throws IOException {
    log.info("Force-adding a pathList view definition.");
    final ViewConfig pathListConfig = store.get("pathList");
    if (pathListConfig == null) {
      store.add(new ViewConfig("pathList", "pathList", new HashMap<String, Object>()));
    }
  }
}
