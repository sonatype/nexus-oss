/*
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

package org.sonatype.nexus.plugins.capabilities.internal.ui;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilitiesPlugin;
import org.sonatype.nexus.plugins.ui.contribution.UiContributionBuilder;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor;

/**
 * Capabilities {@link UiContributor}.
 *
 * @since 2.2.2
 */
@Named
@Singleton
public class CapabilitiesUiContributor
    implements UiContributor
{

  private static final String CAPABILITIES_CSS = "static/css/capabilities.css";

  @Override
  public UiContribution contribute(final boolean debug) {
    UiContributionBuilder builder = new UiContributionBuilder(
        this, CapabilitiesPlugin.GROUP_ID, CapabilitiesPlugin.ARTIFACT_ID
    );
    if (debug) {
      builder.withDependency("css!" + CAPABILITIES_CSS + builder.getCacheBuster(CAPABILITIES_CSS));
    }
    return builder.build(debug);
  }
}
