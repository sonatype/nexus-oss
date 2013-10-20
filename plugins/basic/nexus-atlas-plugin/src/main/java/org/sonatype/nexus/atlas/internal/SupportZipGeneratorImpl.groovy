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

import com.google.common.io.CountingOutputStream
import org.sonatype.nexus.atlas.SupportBundle
import org.sonatype.nexus.atlas.SupportBundle.ContentSource
import org.sonatype.nexus.atlas.SupportBundle.ContentSource.Type
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
import java.util.zip.Deflater
import java.util.zip.ZipEntry

import static com.google.common.base.Preconditions.checkNotNull
import static org.sonatype.nexus.atlas.SupportBundle.ContentSource.Type.*

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
  // FIXME: This is fixed in latest goodies

  // NOTE: max is 30mb, using 28mb for fudge buffer

  @Inject
  SupportZipGeneratorImpl(final ApplicationConfiguration applicationConfiguration,
                          final List<SupportBundleCustomizer> bundleCustomizers,
                          final @Named('${atlas.supportZipGenerator.maxZipFileSize:-28m}') ByteSize maxZipFileSize)
  {
    assert applicationConfiguration
    this.bundleCustomizers = checkNotNull(bundleCustomizers)

    // resolve where support archives will be stored
    supportDir = applicationConfiguration.getWorkingDirectory('support')
    log.info 'Support directory: {}', supportDir

    // FIXME: Need to sort this out, see notes below
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
  private Set<Type> includedTypes(final Request request) {
    def types = []
    if (request.systemInformation) {
      types << SYSINFO
    }
    if (request.threadDump) {
      types << THREAD
    }
    if (request.metrics) {
      types << METRICS
    }
    if (request.configurationFiles) {
      types << CONFIG
    }
    if (request.securityFiles) {
      types << SECURITY
    }
    if (request.logFiles) {
      types << LOG
    }
    return types
  }

  /**
   * Filter only included content sources.
   */
  private List<ContentSource> filterSources(final Request request, final SupportBundle supportBundle) {
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

  @Override
  File generate(final Request request) {
    assert request

    log.info 'Generating support ZIP: {}', request

    def bundle = new SupportBundle()

    // customize the bundle
    bundleCustomizers.each {
      log.debug 'Customizing bundle with: {}', it
      it.customize(bundle)
    }
    assert !bundle.sources.isEmpty(): 'At least one bundle source must be configured'

    // filter only sources which user requested
    def sources = filterSources(request, bundle)
    assert !sources.isEmpty(): 'At least one content source must be configured'

    try {
      // prepare bundle sources
      sources.each {
        log.debug 'Preparing bundle source: {}', it
        it.prepare()
      }

      return createZip(sources, request.limitSize)
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
  private File createZip(final List<ContentSource> sources, final boolean limitSize) {
    def prefix = uniquePrefix()

    // Write zip to temporary file first
    def file = File.createTempFile("${prefix}-", '.zip').canonicalFile
    log.debug 'Writing ZIP file: {}', file

    // track total compressed and uncompressed size
    def stream = new CountingOutputStream(file.newOutputStream())
    long totalUncompressed = 0

    // setup zip too sync-flush so we can detect compressed size for partially written files
    def zip = new FlushableZipOutputStream(stream)
    zip.level = Deflater.DEFAULT_COMPRESSION
    zip.syncFlush = true

    def percentCompressed = { long compressed, long uncompressed ->
      100 - ((compressed / uncompressed) * 100) as int
    }

    // helper to create normalized entry with prefix
    def addEntry = { String path ->
      if (!path.startsWith('/')) {
        path = '/' + path
      }
      def entry = new ZipEntry(prefix + path)
      zip.putNextEntry(entry)
      return entry
    }

    // helper to close entry
    def closeEntry = { ZipEntry entry ->
      zip.closeEntry()
      // not all entries have a size
      if (entry.size) {
        if (log.debugEnabled) {
          log.debug 'Entry (in={} out={}) bytes, compressed: {}%',
              entry.size,
              entry.compressedSize,
              percentCompressed(entry.compressedSize, entry.size)
        }
        totalUncompressed += entry.size
      }
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
          for (int l = path.size(); l > 1; l--) {
            dirs << path[0..-l].join('/')
          }
        }
      }
      dirs.sort().each {
        log.debug 'Adding directory entry: {}', it
        def entry = addEntry "${it}/"
        // must end with '/'
        closeEntry entry
      }
    }

    try {
      // add directory entries
      addDirectoryEntries()

      // TODO: Sort out how to deal with obfuscation, if its specific or general
      // TODO: ... this should be a detail of the content source

      // add content entries, sorted so highest priority are processed first
      sources.sort().each { source ->
        log.debug 'Adding content entry: {} {} bytes', source, source.size
        def entry = addEntry source.path

        // FIXME: Need to decide if we want to do this or not, as its very fudgy and complex
        // FIXME: May be fine to simply limit _each_ included file to a max size
        // FIXME: ... and then if we find we are constantly making zip larger > max upload can support
        // FIXME: ... we can then revisit this?

        source.content.eachByte(4 * 1024) { byte[] buff, len ->
          // TODO: check if there is enough room in the compressed file for given bytes
          // TODO: this is not going to be super accurate, but on small buffer scale
          // TODO: should be plenty to allow us to know when the compressed file is full?
          zip.write(buff)

          // flush so we can detect compressed size for partially written files
          zip.flush()
        }

        closeEntry entry
      }
    }
    finally {
      zip.close()
    }

    if (log.debugEnabled) {
      log.debug 'ZIP (in={} out={}) bytes, compressed: {}%',
          totalUncompressed,
          stream.count,
          percentCompressed(stream.count, totalUncompressed)
    }

    // Move the file into place
    def target = new File(supportDir, "${prefix}.zip")
    Files.move(file.toPath(), target.toPath())
    log.info 'Created support ZIP file: {}', target
    return target
  }
}