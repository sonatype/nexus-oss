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

package org.sonatype.nexus.atlas.internal

import org.sonatype.nexus.atlas.SupportBundle
import org.sonatype.nexus.atlas.SupportBundleCustomizer
import org.sonatype.nexus.atlas.SupportZipGenerator
import org.sonatype.nexus.atlas.SupportZipGenerator.Request
import org.sonatype.nexus.configuration.application.ApplicationConfiguration
import org.sonatype.sisu.goodies.common.ByteSize
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicLong

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Default {@link SupportZipGenerator}.
 *
 * @since 2.7
 */
@Named
@Singleton
class SupportZipGeneratorImpl
extends ComponentSupport
implements SupportZipGenerator
{
  private final List<SupportBundleCustomizer> bundleCustomizers

  private final File supportDir

  private final ByteSize maxZipFileSize

  // FIXME: "30mb" is failing to parse, need to look at the parsing logic again and see WTF is going on :-(

  @Inject
  SupportZipGeneratorImpl(final ApplicationConfiguration applicationConfiguration,
                          final List<SupportBundleCustomizer> bundleCustomizers,
                          final @Named('${atlas.supportZipGenerator.maxZipFileSize:-30m}') ByteSize maxZipFileSize)
  {
    assert applicationConfiguration
    this.bundleCustomizers = checkNotNull(bundleCustomizers)

    // resolve where support archives will be stored
    supportDir = applicationConfiguration.getWorkingDirectory('support')
    log.info 'Support directory: {}', supportDir

    this.maxZipFileSize = maxZipFileSize
    log.info 'Maximum ZIP file size: {}', maxZipFileSize
  }

  @Override
  File getDirectory() {
    return supportDir
  }

  private static final AtomicLong counter = new AtomicLong()

  /**
   * Generate a unique file where the support ZIP will be saved.
   */
  private File uniqueFile() {
    return new File(supportDir, "support-${System.currentTimeMillis()}-${counter.incrementAndGet()}.zip")
  }

  @Override
  File generate(final Request request) {
    assert request

    log.info 'Generating support zip: {}', request

    def bundle = new SupportBundle()

    // customize the bundle
    bundleCustomizers.each {
      log.debug 'Customizing bundle with: {}', it
      it.customize(bundle)
    }

    assert !bundle.sources.isEmpty() : 'At least one bundle source must be configured'

    try {
      // prepare bundle sources
      bundle.sources.each {
        log.debug 'Preparing bundle source: {}', it
        it.prepare()
      }

      // generate ZIP
      // TODO

      return uniqueFile()
    }
    finally {
      // cleanup bundle sources
      bundle.sources.each {
        log.debug 'Cleaning bundle source: {}', it
        try {
          it.cleanup()
        }
        catch (Exception e) {
          log.warn('Bundle source cleanup failed', e)
        }
      }
    }
  }
}