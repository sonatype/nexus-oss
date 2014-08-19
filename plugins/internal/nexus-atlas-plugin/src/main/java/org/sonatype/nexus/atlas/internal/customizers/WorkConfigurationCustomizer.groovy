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

package org.sonatype.nexus.atlas.internal.customizers

import org.sonatype.nexus.configuration.application.ApplicationDirectories
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer
import org.sonatype.nexus.supportzip.FileContentSourceSupport
import org.sonatype.nexus.supportzip.GeneratedContentSourceSupport
import org.sonatype.nexus.supportzip.SupportBundle
import org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Priority
import org.sonatype.nexus.supportzip.SupportBundleCustomizer
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import static com.google.common.base.Preconditions.checkNotNull
import static groovy.io.FileType.FILES
import static org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Priority.DEFAULT
import static org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Priority.LOW
import static org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Priority.REQUIRED
import static org.sonatype.nexus.supportzip.SupportBundle.ContentSource.Type.CONFIG

/**
 * Adds work directory configuration files to support bundle.
 *
 * @since 3.0
 */
@Named
@Singleton
class WorkConfigurationCustomizer
    extends ComponentSupport
    implements SupportBundleCustomizer
{
  private final ApplicationDirectories applicationDirectories

  @Inject
  WorkConfigurationCustomizer(final ApplicationDirectories applicationDirectories) {
    this.applicationDirectories = checkNotNull(applicationDirectories)
  }

  @Override
  void customize(final SupportBundle supportBundle) {
    supportBundle << new NexusXmlContentSource()

    // helper to include a file
    def maybeIncludeFile = { File file, String prefix, Priority priority = DEFAULT ->
      if (file.exists()) {
        log.debug 'Including file: {}', file
        supportBundle << new FileContentSourceSupport(CONFIG, "$prefix/${file.name}", file, priority)
      }
      else {
        log.debug 'Skipping non-existent file: {}', file
      }
    }

    def configDir = applicationDirectories.getWorkDirectory('etc')
    assert configDir.exists()
    configDir.eachFileMatch FILES, ~/logback.*/, {
      maybeIncludeFile it, 'work/etc', LOW
    }
  }

  /**
   * Source for obfuscated nexus.xml
   */
  private class NexusXmlContentSource
      extends GeneratedContentSourceSupport
  {
    NexusXmlContentSource() {
      super(CONFIG, 'work/etc/nexus.xml', REQUIRED)
    }

    @Override
    protected void generate(final File file) {
      def source = new File(applicationDirectories.workDirectory, 'etc/nexus.xml')
      if (!source.exists()) {
        log.debug 'Skipping non-existent file: {}', source
        return
      }

      log.debug 'Reading: {}', source
      source.withInputStream { input ->
        def model = new NexusConfigurationXpp3Reader().read(input)

        // obfuscate sensitive content
        model.smtpConfiguration?.password = PASSWORD_TOKEN
        model.remoteProxySettings?.httpProxySettings?.authentication?.password = PASSWORD_TOKEN
        model.remoteProxySettings?.httpsProxySettings?.authentication?.password = PASSWORD_TOKEN
        model.repositories?.each { repo ->
          repo.remoteStorage?.authentication?.password = PASSWORD_TOKEN
        }

        file.withOutputStream { output ->
          new NexusConfigurationXpp3Writer().write(output, model)
        }
      }
    }
  }
}
