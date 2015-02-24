/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.createrepo

import com.google.common.io.BaseEncoding
import com.google.common.io.CountingInputStream
import org.redline_rpm.ReadableChannelWrapper
import org.redline_rpm.header.AbstractHeader
import org.redline_rpm.header.AbstractHeader.Tag
import org.redline_rpm.header.Flags
import org.redline_rpm.header.Format
import org.redline_rpm.header.Header
import org.redline_rpm.header.Signature

import java.nio.channels.Channels
import java.security.DigestInputStream
import java.security.MessageDigest

import static org.redline_rpm.ChannelWrapper.Key
import static org.redline_rpm.header.Header.HeaderTag

/**
 * Yum metadata parser.
 *
 * @since 3.0
 */
class YumPackageParser
{

  /**
   * Parse yum metadata out of an rpm.
   *
   * @param rpm rpm content
   * @param location location where rpm will be accessible
   * @param lastModified last modified time of rpm file (in seconds)
   * @return parsed yum metadata
   */
  YumPackage parse(final InputStream rpm, final String location, final long lastModified) {
    YumPackage yumPackage = null
    def countingStream = new CountingInputStream(rpm)
    def digestStream = new DigestInputStream(countingStream, MessageDigest.getInstance('SHA-256'))
    digestStream.withStream { InputStream rpmStream ->
      ReadableChannelWrapper rcw = new ReadableChannelWrapper(Channels.newChannel(rpmStream))
      rcw.withCloseable {
        Format format = read(rcw)
        Signature signature = format.signature
        Header header = format.header

        // read all remaining content so we get proper size/checksum
        byte[] buffer = new byte[1024]
        while (rpmStream.read(buffer) != -1) {
        }

        yumPackage = fixRequires(new YumPackage(
            location: location,
            checksumType: 'sha256',
            name: asString(header, HeaderTag.NAME),
            arch: asString(header, HeaderTag.ARCH),
            version: asString(header, HeaderTag.VERSION),
            epoch: asString(header, HeaderTag.EPOCH),
            release: asString(header, HeaderTag.RELEASE),
            summary: asString(header, HeaderTag.SUMMARY),
            description: asString(header, HeaderTag.DESCRIPTION),
            url: asString(header, HeaderTag.URL),
            timeFile: lastModified,
            timeBuild: asInt(header, HeaderTag.BUILDTIME),
            rpmLicense: asString(header, HeaderTag.LICENSE),
            rpmVendor: asString(header, HeaderTag.VENDOR),
            rpmGroup: asString(header, HeaderTag.GROUP),
            rpmBuildHost: asString(header, HeaderTag.BUILDHOST),
            rpmSourceRpm: asString(header, HeaderTag.SOURCERPM),
            rpmHeaderStart: signature.getEndPos() + header.getStartPos(),
            rpmHeaderEnd: header.getEndPos(),
            packager: asString(header, HeaderTag.PACKAGER),
            sizeInstalled: asInt(header, HeaderTag.SIZE),
            sizeArchive: asInt(signature, Signature.SignatureTag.PAYLOADSIZE),
            provides: parsePCO(header, HeaderTag.PROVIDENAME, HeaderTag.PROVIDEVERSION, HeaderTag.PROVIDEFLAGS),
            requires: parsePCO(header, HeaderTag.REQUIRENAME, HeaderTag.REQUIREVERSION, HeaderTag.REQUIREFLAGS),
            conflicts: parsePCO(header, HeaderTag.CONFLICTNAME, HeaderTag.CONFLICTVERSION, HeaderTag.CONFLICTFLAGS),
            obsoletes: parsePCO(header, HeaderTag.OBSOLETENAME, HeaderTag.OBSOLETEVERSION, HeaderTag.OBSOLETEFLAGS),
            files: parseFiles(header),
            changes: parseChanges(header)
        ))
      }
    }
    yumPackage.with {
      checksum = BaseEncoding.base16().lowerCase().encode(digestStream.messageDigest.digest())
      pkgId = checksum
      sizePackage = countingStream.count
    }
    return yumPackage
  }

  /**
   * Parse files headers.
   */
  def parseFiles(final Header header) {
    def files = []
    def names = header.getEntry(HeaderTag.BASENAMES)?.values
    if (!names) {
      return null
    }
    def dirnames = header.getEntry(HeaderTag.DIRNAMES)?.values
    def dirindexes = header.getEntry(HeaderTag.DIRINDEXES)?.values
    def fileflags = header.getEntry(HeaderTag.FILEFLAGS)?.values
    def filemodes = header.getEntry(HeaderTag.FILEMODES)?.values

    names.eachWithIndex { name, i ->
      String path = dirnames[dirindexes[i]] + name
      def type = YumPackage.FileType.file
      def flag = fileflags[i]
      // signed to unsigned
      def mode = filemodes[i] + 0xFFFF + 1
      if (mode & 0x4000) {
        type = YumPackage.FileType.dir
      }
      else if (flag & 0x40) {
        type = YumPackage.FileType.ghost
      }
      files << new YumPackage.File(
          name: path,
          type: type,
          primary: (type != YumPackage.FileType.ghost) && (path.contains('bin/') || path.startsWith('/etc/') || (type == YumPackage.FileType.file && path == '/usr/lib/sendmail'))
      )
    }
    // sort by name ASC
    files.sort { a, b -> a.name.compareTo(b.name) }
    return files
  }

  /**
   * Parse provides/requires/conflicts/obsoletes headers.
   */
  def parsePCO(final Header header, final HeaderTag namesTag, final HeaderTag versionTag, final HeaderTag flagsTag) {
    def provides = []
    def names = header.getEntry(namesTag)?.values
    if (!names) {
      return null
    }
    def versions = header.getEntry(versionTag)?.values
    def flags = header.getEntry(flagsTag)?.values
    names.eachWithIndex { name, i ->
      def epoch, version, release
      (epoch, version, release) = parseVersion(versions[i])
      def flag = flags[i]
      provides << new YumPackage.Entry(
          name: name,
          epoch: epoch,
          version: version,
          release: release,
          flags: parsePCOFlags(flag),
          pre: flag & (Flags.PREREQ | Flags.SCRIPT_PRE | Flags.SCRIPT_POST)
      )
    }
    // sort by name ASC
    provides.sort { a, b -> a.name.compareTo(b.name) }
    return provides
  }

  /**
   * Parse PCO flags (LT/GT/EQ/LE/GE).
   */
  def parsePCOFlags(final Integer flags) {
    def workFlags = flags & 0xf
    if (workFlags == 2) {
      return 'LT'
    }
    else if (workFlags == 4) {
      return 'GT'
    }
    else if (workFlags == 8) {
      return 'EQ'
    }
    else if (workFlags == 10) {
      return 'LE'
    }
    else if (workFlags == 12) {
      return 'GE'
    }
    return null
  }

  /**
   * Parse change log headers.
   */
  def parseChanges(final Header header) {
    def changes = []
    def names = header.getEntry(HeaderTag.CHANGELOGNAME)?.values
    if (!names) {
      return null
    }
    def dates = header.getEntry(HeaderTag.CHANGELOGTIME)?.values
    def texts = header.getEntry(HeaderTag.CHANGELOGTEXT)?.values
    names.eachWithIndex { name, i ->
      changes << new YumPackage.ChangeLog(
          author: names[i],
          date: dates[i],
          text: texts[i]
      )
    }
    // sort by date ASC
    changes.sort { a, b -> a.date.compareTo(b.date) }
    return changes
  }

  /**
   * Fix requires (according to fixes they apply in original createrepo).
   */
  def fixRequires(final YumPackage yumPackage) {
    if (yumPackage.requires) {
      def provideNames = yumPackage.provides?.collectEntries { [it.name, it] }
      def fileNames = yumPackage.files?.collect { it.name }
      yumPackage.requires = yumPackage.requires.findResults { YumPackage.Entry item ->
        if (item.name.startsWith('rpmlib(')) {
          return null
        }
        // requires a file included in rpm
        if (item.name.startsWith('/') && fileNames.contains(item.name) && !item.flags) {
          return null
        }
        // require something the rpm provides
        if (provideNames.containsKey(item.name)) {
          if (!item.flags) {
            return null
          }
          YumPackage.Entry provide = provideNames.get(item.name)
          if (item.epoch == provide.epoch && item.version == provide.version && item.release == provide.release) {
            return null
          }
        }
        return item
      }
    }
    return yumPackage
  }

  /**
   * Parse a version string into epoch/ver/rel.
   */
  def parseVersion(final String fullVersion) {
    def epoch = null, version = null, release = null
    if (fullVersion) {
      def i = fullVersion.indexOf(':')
      if (i != -1) {
        try {
          epoch = String.valueOf(fullVersion.substring(0, i))
        }
        catch (NumberFormatException e) {
          epoch = '0'
        }
      }
      def j = fullVersion.indexOf('-')
      if (j != -1) {
        release = fullVersion.substring(j + 1)
        version = fullVersion.substring(Math.max(i + 1, 0), j)
      }
      else {
        version = fullVersion.substring(Math.max(i + 1, 0))
      }
    }
    return [epoch, version, release]
  }

  /**
   * Parse a rpm.
   */
  def read(ReadableChannelWrapper content) throws IOException {
    Format format = new Format()

    Key<Integer> headerStartKey = content.start()

    Key<Integer> leadStartKey = content.start()
    format.lead.read(content)
    content.finish(leadStartKey)

    Key<Integer> signatureStartKey = content.start()
    format.signature.read(content)
    content.finish(signatureStartKey)

    Integer headerStartPos = content.finish(headerStartKey)
    format.header.setStartPos(headerStartPos)
    Key<Integer> headerKey = content.start()
    format.header.read(content)
    Integer headerLength = content.finish(headerKey)
    format.header.setEndPos(headerStartPos + headerLength)

    return format
  }

  /**
   * Get value of a tag as string.
   */
  def asString(final AbstractHeader header, final Tag tag) {
    AbstractHeader.Entry<?> entry = header.getEntry(tag)
    if (!entry) {
      return null
    }
    Object values = entry.values
    return values[0]
  }

  /**
   * Get value of a tag as integer.
   */
  def asInt(final AbstractHeader header, final Tag tag) {
    AbstractHeader.Entry<?> entry = header.getEntry(tag)
    if (!entry) {
      return 0
    }
    Object values = entry.values
    return values[0]
  }

}
