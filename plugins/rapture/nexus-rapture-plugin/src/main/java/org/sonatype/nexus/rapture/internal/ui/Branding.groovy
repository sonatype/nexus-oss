/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rapture.internal.ui

import org.sonatype.nexus.rapture.StateContributor
import org.sonatype.nexus.rapture.internal.capability.BrandingCapabilityConfiguration
import org.sonatype.nexus.web.BaseUrlHolder

import javax.inject.Named
import javax.inject.Singleton
import java.util.regex.Matcher

/**
 * Branding state contributor.
 *
 * @since 3.0
 */
@Named
@Singleton
class Branding
implements StateContributor
{

  private BrandingCapabilityConfiguration config

  @Override
  Map<String, Object> getState() {
    Map<String, Object> state = null
    if (config) {
      state = ['branding': new BrandingXO(
          headerEnabled: config.headerEnabled,
          headerHtml: config.headerHtml ? config.headerHtml.replaceAll(Matcher.quoteReplacement('$baseUrl'), BaseUrlHolder.get()) : null,
          footerEnabled: config.footerEnabled,
          footerHtml: config.footerHtml ? config.footerHtml.replaceAll(Matcher.quoteReplacement('$baseUrl'), BaseUrlHolder.get()) : null
      )];
    }
    return state;
  }

  @Override
  Map<String, Object> getCommands() {
    return null
  }

  public void set(final BrandingCapabilityConfiguration config) {
    this.config = config
  }

  public void reset() {
    this.config = null
  }

}
