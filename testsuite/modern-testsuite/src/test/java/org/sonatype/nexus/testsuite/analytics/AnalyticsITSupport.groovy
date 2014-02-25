/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
