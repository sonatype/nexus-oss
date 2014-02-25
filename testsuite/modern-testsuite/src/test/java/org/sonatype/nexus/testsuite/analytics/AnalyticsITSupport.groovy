/**
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.analytics

import org.junit.Rule
import org.junit.rules.ExpectedException
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration
import org.sonatype.nexus.testsuite.NexusCoreITSupport
import org.sonatype.nexus.testsuite.analytics.client.Events
import org.sonatype.nexus.testsuite.analytics.client.Events.EventsXO
import org.sonatype.nexus.testsuite.analytics.client.Settings

/**
 * Analytics ITs support class.
 *
 * @since 2.8
 */
class AnalyticsITSupport
extends NexusCoreITSupport
{

  @Rule
  public ExpectedException thrown = ExpectedException.none()

  AnalyticsITSupport(String nexusBundleCoordinates) {
    super(nexusBundleCoordinates)
  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    return super.configureNexus(configuration).addPlugins(
        artifactResolver().resolvePluginFromDependencyManagement(
            'org.sonatype.nexus.plugins', 'nexus-analytics-plugin'
        )
    );
  }

  protected Settings getSettings() {
    return client().getSubsystem(Settings.class);
  }

  protected Events getEvents() {
    return client().getSubsystem(Events.class);
  }

  protected void configureAnalytics(boolean collection, boolean autosubmit) {
    logger.info "Configuring analytics: collection=${collection}, autosubmit=${autosubmit}"
    settings.set(new Settings.SettingsXO(
        collection: collection,
        autosubmit: autosubmit
    ))
  }

  protected EventsXO getAllEvents() {
    return events.get(0, Integer.MAX_VALUE)
  }

  protected List<String> pathsOf(EventsXO events) {
    return events.events.collect { "${it.attributes['method']}|${it.attributes['path']}" as String }
  }

}
