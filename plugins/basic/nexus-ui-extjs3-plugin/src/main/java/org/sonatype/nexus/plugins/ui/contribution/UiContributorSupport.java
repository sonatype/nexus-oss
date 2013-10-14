package org.sonatype.nexus.plugins.ui.contribution;

import org.sonatype.nexus.plugin.PluginIdentity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link UiContributor} implementations.
 *
 * @since 2.7
 */
public class UiContributorSupport
    implements UiContributor
{
  private final PluginIdentity owner;

  public UiContributorSupport(final PluginIdentity owner) {
    this.owner = checkNotNull(owner);
  }

  @Override
  public UiContribution contribute(final boolean debug) {
    UiContributionBuilder builder = new UiContributionBuilder(owner);

    if (debug) {
      // include debug stylesheet if there is one for the plugin
      String css = String.format("static/css/%s.css", owner.getCoordinates().getArtifactId());
      if (owner.getClass().getClassLoader().getResource(css) != null) {
        builder.withDependency("css!" + css + builder.getCacheBuster(css));
      }
    }

    return builder.build(debug);
  }
}
