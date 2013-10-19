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
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicLong
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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

  /**
   * Return set of included content source types.
   */
  private Set<SupportBundle.ContentSource.Type> includedTypes(final Request request) {
    def include = []
    if (request.systemInformation) {
      include << SupportBundle.ContentSource.Type.SYSINFO
    }
    if (request.threadDump) {
      include << SupportBundle.ContentSource.Type.THREAD
    }
    if (request.metrics) {
      include << SupportBundle.ContentSource.Type.METRICS
    }
    if (request.configurationFiles) {
      include << SupportBundle.ContentSource.Type.CONFIG
    }
    if (request.securityFiles) {
      include << SupportBundle.ContentSource.Type.SECURITY
    }
    if (request.logFiles) {
      include << SupportBundle.ContentSource.Type.LOG
    }
    return include
  }

  /**
   * Filter only included content sources.
   */
  private List<SupportBundle.ContentSource> filterSources(final Request request, final SupportBundle supportBundle) {
    def include = includedTypes(request)
    def sources = []
    supportBundle.sources.each {
      if (include.contains(it.type)) {
        log.debug 'Including content source: {}', it
        sources << it
      }
    }
    return sources
  }

  private static final AtomicLong counter = new AtomicLong()

  /**
   * Generate a unique file prefix.
   */
  private String uniquePrefix() {
    // TODO: Consider using a dateformat here instead?
    return "support-${System.currentTimeMillis()}-${counter.incrementAndGet()}"
  }

  /**
   * Create a ZIP file with content from given sources.
   */
  private File createZip(final List<SupportBundle.ContentSource> sources) {
    def prefix = uniquePrefix()

    // Write zip to temporary file first
    def file = File.createTempFile("${prefix}-", '.zip')
    log.debug 'Writing ZIP file: {}', file

    def zip = new ZipOutputStream(file.newOutputStream())
    AtomicLong zipSize = new AtomicLong(0)

    // helper to create normalized entry with prefix
    def addEntry = { String path ->
      if (!path.startsWith('/')) {
        path = '/' + path
      }
      def entry = new ZipEntry(prefix + path)
      zip.putNextEntry(entry)
      return entry
    }

    // helper to track the size of the zip file
    def trackSize = { ZipEntry entry ->
      zip.closeEntry()
      def size = entry.compressedSize
      zipSize.addAndGet(size)
      log.debug 'Entry size: {}', size
    }

    // helper to add entries for each directory
    def addDirectoryEntries = {
      // include entry for top-level directory
      addEntry '/'

      // add unique directory entries
      Set<String> dirs = []
      sources.each {
        def path = it.path.split('/') as List
        if (path.size() > 1) {
          // eg. 'foo/bar/baz' -> [ 'foo', 'foo/bar' ]
          for (int l=path.size(); l>1; l--) {
            dirs << path[0..-l].join('/')
          }
        }
      }
      dirs.sort().each {
        log.debug 'Adding directory entry: {}', it
        def entry = addEntry "${it}/" // must end with '/'
        trackSize entry
      }
    }

    try {
      // add directory entries
      addDirectoryEntries()

      // TODO: handle truncation... how?
      // TODO: Perhaps add truncatable flag to source, apply all non-truncateables first
      // TODO: then add api to allow specific size of content

      // add content entries
      sources.each { source ->
        log.debug 'Adding content entry: {}; size: {}', source.path, source.size
        def entry = addEntry source.path
        source.content.withStream {
          zip << it
        }
        trackSize entry
      }

      log.debug 'ZIP size: {}', zipSize
    }
    finally {
      zip.close()
    }

    // Move the file into place
    def target = new File(supportDir, "${prefix}.zip")
    Files.move(file.toPath(), target.toPath())
    log.info 'Created support ZIP file: {}', target
    return target
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

    def sources = filterSources(request, bundle)
    // TODO: What do we do if there are no sources?

    try {
      // prepare bundle sources
      sources.each {
        log.debug 'Preparing bundle source: {}', it
        it.prepare()
      }

      return createZip(sources)
    }
    catch (Exception e) {
      log.error 'Failed to create support ZIP', e
    }
    finally {
      // cleanup bundle sources
      sources.each {
        log.debug 'Cleaning bundle source: {}', it
        try {
          it.cleanup()
        }
        catch (Exception e) {
          log.warn 'Bundle source cleanup failed', e
        }
      }
    }
  }
}