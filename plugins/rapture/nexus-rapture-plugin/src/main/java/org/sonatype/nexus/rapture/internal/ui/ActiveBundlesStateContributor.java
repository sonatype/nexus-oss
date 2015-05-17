package org.sonatype.nexus.rapture.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.rapture.StateContributor;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.ImmutableMap;
import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.bundle.core.BundleState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contributes {@code activeBundles} state.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ActiveBundlesStateContributor
  extends ComponentSupport
  implements StateContributor
{
  private final BundleContext bundleContext;

  private final BundleService bundleService;

  @Inject
  public ActiveBundlesStateContributor(final BundleContext bundleContext,
                                       final BundleService bundleService)
  {
    this.bundleContext = checkNotNull(bundleContext);
    this.bundleService = checkNotNull(bundleService);
  }

  @Override
  public Map<String, Object> getState() {
    return ImmutableMap.of("activeBundles", calculateActivateBundles());
  }

  @Override
  public Map<String, Object> getCommands() {
    return null;
  }

  // TODO: sort out how we can cache/rebuild this, to avoid doing this every status poll

  /**
   * Returns sorted list of activate bundle symbolic-names to use for UI feature activation.
   */
  private Object calculateActivateBundles() {
    log.info("Determining active bundles");

    List<String> bundles = new ArrayList<>();

    for (Bundle bundle : bundleContext.getBundles()) {
      BundleInfo info = bundleService.getInfo(bundle);
      String name = info.getSymbolicName();

      // ignore wrapped bundles
      if (name.startsWith("wrap_mvn")) {
        continue;
      }

      // TODO: Should we strip out any others?

      // only report active bundles
      if (info.getState() == BundleState.Active) {
        bundles.add(name);
      }
    }

    Collections.sort(bundles);
    return bundles;
  }
}
