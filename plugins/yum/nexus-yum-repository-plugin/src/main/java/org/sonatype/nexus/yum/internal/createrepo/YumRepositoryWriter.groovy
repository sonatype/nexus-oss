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
import com.google.common.io.CountingOutputStream
import javanet.staxutils.IndentingXMLStreamWriter
import org.apache.commons.io.IOUtils

import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream

/**
 * Support class for writing yum repository metadata.
 *
 * @since 3.0
 */
abstract class YumRepositoryWriter
implements Closeable
{

  protected File repoDir
  protected int timestamp
  private File groupFile
  private Output po
  private Output fo
  private Output oo
  protected XMLStreamWriter pw
  protected XMLStreamWriter fw
  protected XMLStreamWriter ow
  protected XMLStreamWriter rw
  private boolean open
  private boolean closed

  YumRepositoryWriter(final File repoDir, final Integer timestamp = null, final File groupFile = null) {
    this.repoDir = repoDir
    this.timestamp = timestamp ?: System.currentTimeMillis() / 1000
    this.groupFile = groupFile
    XMLOutputFactory factory = XMLOutputFactory.newInstance()
    po = new Output(new FileOutputStream(new File(repoDir, 'primary.xml.gz')))
    pw = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(po.stream, "UTF-8"))

    fo = new Output(new FileOutputStream(new File(repoDir, 'filelists.xml.gz')))
    fw = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(fo.stream, "UTF-8"))

    oo = new Output(new FileOutputStream(new File(repoDir, 'other.xml.gz')))
    ow = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(oo.stream, "UTF-8"))

    rw = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(new FileOutputStream(new File(repoDir, 'repomd.xml')), "UTF-8"))
  }

  /**
   * Write a package into primary.xml.
   */
  protected void writePrimary(final YumPackage yumPackage) {
    pw.writeStartElement('package')
    pw.writeAttribute('type', 'rpm')
    writeBase(yumPackage)
    writeFormat(yumPackage)
    pw.writeEndElement()
  }

  /**
   * Write a package into filelists.xml.
   */
  protected void writeFileLists(final YumPackage yumPackage) {
    fw.writeStartElement('package')
    fw.writeAttribute('pkgid', yumPackage.pkgId)
    fw.writeAttribute('name', yumPackage.name)
    fw.writeAttribute('arch', yumPackage.arch)
    writeEl(fw, 'version', ['epoch': yumPackage.epoch, 'ver': yumPackage.version, 'rel': yumPackage.release])
    writeFiles(fw, yumPackage, false)
    fw.writeEndElement()
  }

  /**
   * Write a package into other.xml.
   */
  protected void writeOther(final YumPackage yumPackage) {
    ow.writeStartElement('package')
    ow.writeAttribute('pkgid', yumPackage.pkgId)
    ow.writeAttribute('name', yumPackage.name)
    ow.writeAttribute('arch', yumPackage.arch)
    yumPackage.changes.each { changelog ->
      writeEl(ow, 'changelog', changelog.text, ['author': changelog.author, 'date': changelog.date])
    }
    ow.writeEndElement()
  }

  /**
   * Write base section of a package into primary.xml.
   */
  private void writeBase(final YumPackage yumPackage) {
    writeEl(pw, 'name', yumPackage.name)
    writeEl(pw, 'arch', yumPackage.arch)
    writeEl(pw, 'version', ['epoch': yumPackage.epoch, 'ver': yumPackage.version, 'rel': yumPackage.release])
    writeEl(pw, 'checksum', yumPackage.checksum, ['type': yumPackage.checksumType, 'pkgid': 'YES'])
    writeEl(pw, 'summary', yumPackage.summary)
    writeEl(pw, 'description', yumPackage.description)
    writeEl(pw, 'packager', yumPackage.packager)
    writeEl(pw, 'url', yumPackage.url)
    writeEl(pw, 'time', ['file': yumPackage.timeFile, 'build': yumPackage.timeBuild])
    writeEl(pw, 'size', ['package': yumPackage.sizePackage, 'installed': yumPackage.sizeInstalled, 'archive': yumPackage.sizeArchive])
    writeEl(pw, 'location', ['href': yumPackage.location])
  }

  /**
   * Write format section of a package into primary.xml.
   */
  private void writeFormat(final YumPackage yumPackage) {
    pw.writeStartElement('format')
    writeEl(pw, 'rpm:license', yumPackage.rpmLicense)
    writeEl(pw, 'rpm:vendor', yumPackage.rpmVendor)
    writeEl(pw, 'rpm:group', yumPackage.rpmGroup)
    writeEl(pw, 'rpm:buildhost', yumPackage.rpmBuildHost)
    writeEl(pw, 'rpm:sourcerpm', yumPackage.rpmSourceRpm)
    writeEl(pw, 'rpm:header-range', ['start': yumPackage.rpmHeaderStart, 'end': yumPackage.rpmHeaderEnd])
    writePCO(yumPackage.provides, 'provides')
    writePCO(yumPackage.requires, 'requires')
    writePCO(yumPackage.conflicts, 'conflicts')
    writePCO(yumPackage.obsoletes, 'obsoletes')
    writeFiles(pw, yumPackage, true)
    pw.writeEndElement()
  }

  /**
   * Write provides/requires/conflicts/obsoletes entries of a package into primary.xml.
   */
  private void writePCO(final List<YumPackage.Entry> entries, final String type) {
    if (entries) {
      pw.writeStartElement('rpm:' + type)
      entries.each { entry ->
        writeEl(pw, 'rpm:entry', [
            'name': entry.name,
            'flags': entry.flags,
            'epoch': entry.epoch, 'ver': entry.version, 'rel': entry.release,
            'pre': entry.pre ? '1' : null
        ])
      }
      pw.writeEndElement()
    }
  }

  /**
   * Write files entries of a package into primary.xml or filelists.xml.
   */
  private void writeFiles(final XMLStreamWriter writer, final YumPackage yumPackage, final boolean primary) {
    def files = yumPackage.files
    if (files) {
      if (primary) {
        files = files.findResults { YumPackage.File file -> file.primary ? file : null }
      }
      files.findResults { YumPackage.File file -> file.type == YumPackage.FileType.file ? file : null }.each { file ->
        writeEl(writer, 'file', file.name)
      }
      files.findResults { YumPackage.File file -> file.type == YumPackage.FileType.dir ? file : null }.each { file ->
        writeEl(writer, 'file', file.name, ['type': file.type])
      }
      files.findResults { YumPackage.File file -> file.type == YumPackage.FileType.ghost ? file : null }.each { file ->
        writeEl(writer, 'file', file.name, ['type': file.type])
      }
    }
  }

  /**
   * Write an xml element.
   */
  protected void writeEl(final XMLStreamWriter writer, final String name, final Object text, final Map<String, Object> attributes = null) {
    writer.writeStartElement(name)
    attributes?.each { key, value ->
      if (value) {
        writer.writeAttribute(key, value?.toString())
      }
    }
    if (text) {
      writer.writeCharacters(text.toString())
    }
    writer.writeEndElement()
  }

  /**
   * Write an xml element without text.
   */
  protected void writeEl(final XMLStreamWriter writer, final String name, final Map<String, Object> attributes) {
    writeEl(writer, name, null, attributes)
  }

  /**
   * Write a data entry into repomd.xml.
   */
  private void writeData(final Output output, final String type) {
    rw.writeStartElement('data')
    rw.writeAttribute('type', type)
    writeEl(rw, 'checksum', output.compressedChecksum, ['type': 'sha256'])
    writeEl(rw, 'open-checksum', output.openChecksum, ['type': 'sha256'])
    writeEl(rw, 'location', ['href': "repodata/${type}.xml.gz"])
    writeEl(rw, 'timestamp', timestamp)
    writeEl(rw, 'size', output.compressedSize)
    writeEl(rw, 'open-size', output.openSize)
    rw.writeEndElement()
  }

  /**
   * Write group/group_gz data entries into repomd.xml.
   */
  private void writeGroup() {
    new FileInputStream(groupFile).withStream { InputStream input ->
      new FileOutputStream(new File(repoDir, 'comps.xml')).withStream { OutputStream output ->
        IOUtils.copy(input, output)
      }
    }
    new FileInputStream(groupFile).withStream { InputStream input ->
      Output go = new Output(new FileOutputStream(new File(repoDir, 'comps.xml.gz')))
      go.stream.withStream { OutputStream output ->
        IOUtils.copy(input, output)
      }

      rw.writeStartElement('data')
      rw.writeAttribute('type', 'group')
      writeEl(rw, 'checksum', go.openChecksum, ['type': 'sha256'])
      writeEl(rw, 'location', ['href': 'repodata/comps.xml'])
      writeEl(rw, 'timestamp', timestamp)
      writeEl(rw, 'size', go.openSize)
      rw.writeEndElement()

      rw.writeStartElement('data')
      rw.writeAttribute('type', 'group_gz')
      writeEl(rw, 'checksum', go.compressedChecksum, ['type': 'sha256'])
      writeEl(rw, 'open-checksum', go.openChecksum, ['type': 'sha256'])
      writeEl(rw, 'location', ['href': 'repodata/comps.xml.gz'])
      writeEl(rw, 'timestamp', timestamp)
      writeEl(rw, 'size', go.compressedSize)
      rw.writeEndElement()
    }
  }

  /**
   * Write starts into primary/filelists/other xmls, if was not yet done.
   */
  void maybeStart() {
    if (!open) {
      open = true

      pw.writeStartDocument('UTF-8', '1.0')
      pw.writeStartElement('metadata')
      pw.writeAttribute('xmlns', 'http://linux.duke.edu/metadata/common')
      pw.writeAttribute('xmlns:rpm', 'http://linux.duke.edu/metadata/rpm')

      fw.writeStartDocument('UTF-8', '1.0')
      fw.writeStartElement('filelists')
      fw.writeAttribute('xmlns', 'http://linux.duke.edu/metadata/filelists')

      ow.writeStartDocument('UTF-8', '1.0')
      ow.writeStartElement('otherdata')
      ow.writeAttribute('xmlns', 'http://linux.duke.edu/metadata/other')
    }
  }

  /**
   * Write ends into primary/filelists/other xmls and write the full repomd.xml.
   */
  @Override
  void close() {
    maybeStart()

    assert !closed
    closed = true

    pw.writeEndDocument()
    pw.close()
    po.stream.close()

    fw.writeEndDocument()
    fw.close()
    fo.stream.close()

    ow.writeEndDocument()
    ow.close()
    oo.stream.close()

    rw.writeStartDocument('UTF-8', '1.0')
    rw.writeStartElement('repomd')
    rw.writeAttribute('xmlns', 'http://linux.duke.edu/metadata/repo')
    rw.writeAttribute('xmlns:rpm', 'http://linux.duke.edu/metadata/rpm')
    writeData(po, 'primary')
    writeData(fo, 'filelists')
    writeData(oo, 'other')
    if (groupFile) {
      writeGroup()
    }
    rw.writeEndDocument()
    rw.close()
  }

  /**
   * Holder of streams used to get checksum/size of open and gzip xml content.
   */
  private static class Output
  {
    private CountingOutputStream openSizeStream
    private CountingOutputStream compressedSizeStream
    private DigestOutputStream openDigestStream
    private DigestOutputStream compressedDigestStream
    private String openChecksum
    private String compressedChecksum

    Output(final OutputStream stream) {
      compressedDigestStream = new DigestOutputStream(stream, MessageDigest.getInstance("SHA-256"))
      compressedSizeStream = new CountingOutputStream(compressedDigestStream)
      openDigestStream = new DigestOutputStream(new GZIPOutputStream(compressedSizeStream), MessageDigest.getInstance("SHA-256"))
      openSizeStream = new CountingOutputStream(openDigestStream)
    }

    OutputStream getStream() {
      return openSizeStream
    }

    long getOpenSize() {
      return openSizeStream.count
    }

    long getCompressedSize() {
      return compressedSizeStream.count
    }

    String getOpenChecksum() {
      if (!openChecksum) {
        openChecksum = BaseEncoding.base16().lowerCase().encode(openDigestStream.messageDigest.digest())
      }
      return openChecksum
    }

    String getCompressedChecksum() {
      if (!compressedChecksum) {
        compressedChecksum = BaseEncoding.base16().lowerCase().encode(compressedDigestStream.messageDigest.digest())
      }
      return compressedChecksum
    }
  }

}
