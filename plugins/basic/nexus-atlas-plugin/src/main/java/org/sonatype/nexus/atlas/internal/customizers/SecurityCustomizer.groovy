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

package org.sonatype.nexus.atlas.internal.customizers

import org.sonatype.nexus.atlas.FileContentSourceSupport
import org.sonatype.nexus.atlas.SupportBundle
import org.sonatype.nexus.atlas.SupportBundleCustomizer
import org.sonatype.nexus.configuration.application.ApplicationConfiguration
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import static com.google.common.base.Preconditions.checkNotNull
import static org.sonatype.nexus.atlas.SupportBundle.ContentSource.Type.CONFIG

/**
 * Adds system security files to support bundle.
 *
 * @since 2.7
 */
@Named
@Singleton
class SecurityCustomizer
extends ComponentSupport
implements SupportBundleCustomizer
{
  private final ApplicationConfiguration applicationConfiguration

  @Inject
  SecurityCustomizer(final ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = checkNotNull(applicationConfiguration)
  }

  @Override
  void customize(final SupportBundle supportBundle) {
    // helper to include a file
    def maybeIncludeFile = { File file, String prefix ->
      if (file.exists()) {
        log.debug 'Including file: {}', file
        supportBundle << new FileContentSourceSupport(CONFIG, "$prefix/${file.name}", file)
      }
      else {
        log.trace 'Skipping non-existent file: {}', file
      }
    }

    // include runtime configuration
    def configDir = applicationConfiguration.configurationDirectory
    assert configDir.exists()

    // capture specific files (we don't want .bak, .tmp and other garbage)
    [ 'security.xml', 'security-configuration.xml' ].each {
      maybeIncludeFile new File(configDir, it), 'work/conf'
    }
  }
}