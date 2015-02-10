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

import com.google.common.collect.Sets
import org.sonatype.nexus.yum.internal.RepoMD

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader
import java.util.zip.GZIPInputStream

/**
 * Merges yum repositories (similar to merge repo).
 * @since 3.0
 */
class MergeYumRepository
extends YumRepositoryWriter
{

  private Set<String> writtenPrimary
  private Set<String> writtenFiles
  private Set<String> writtenOther

  MergeYumRepository(final File repoDir, final Integer timestamp = null) {
    super(repoDir, timestamp)
    writtenPrimary = Sets.newHashSet()
    writtenFiles = Sets.newHashSet()
    writtenOther = Sets.newHashSet()
  }

  /**
   * Merge a yum repository.
   */
  void merge(final File repoDir) {
    maybeStart()

    XMLStreamReader pr = null
    XMLStreamReader fr = null
    XMLStreamReader or = null

    try {
      XMLInputFactory factory = XMLInputFactory.newInstance()
      new FileInputStream(new File(repoDir, "repodata/repomd.xml")).withStream { InputStream repoMDIn ->
        RepoMD repoMD = new RepoMD(repoMDIn)
        pr = factory.createXMLStreamReader(new GZIPInputStream(new FileInputStream(new File(repoDir, repoMD.getLocation('primary')))), "UTF-8")
        fr = factory.createXMLStreamReader(new GZIPInputStream(new FileInputStream(new File(repoDir, repoMD.getLocation('filelists')))), "UTF-8")
        or = factory.createXMLStreamReader(new GZIPInputStream(new FileInputStream(new File(repoDir, repoMD.getLocation('other')))), "UTF-8")
      }
      readPrimary(pr)
      readFiles(fr)
      readOther(or)
    }
    finally {
      pr?.close()
      fr?.close()
      or?.close()
    }
  }

  /**
   * Read & merge package metadata from primary.xml.
   */
  def readPrimary(final XMLStreamReader reader) {
    def yumPackage = null, text = null, fileType = null, pco = null
    while (reader.hasNext()) {
      int event = reader.next()
      if (event == XMLStreamConstants.START_ELEMENT) {
        if (reader.localName == 'package') {
          yumPackage = new YumPackage()
        }
        else if (reader.localName == 'version') {
          yumPackage.epoch = reader.getAttributeValue(null, 'epoch')
          yumPackage.version = reader.getAttributeValue(null, 'ver')
          yumPackage.release = reader.getAttributeValue(null, 'rel')
        }
        else if (reader.localName == 'checksum') {
          yumPackage.checksumType = reader.getAttributeValue(null, 'type')
        }
        else if (reader.localName == 'time') {
          yumPackage.timeFile = reader.getAttributeValue(null, 'file') as Integer
          yumPackage.timeBuild = reader.getAttributeValue(null, 'build') as Integer
        }
        else if (reader.localName == 'size') {
          yumPackage.sizePackage = reader.getAttributeValue(null, 'package') as Integer
          yumPackage.sizeInstalled = reader.getAttributeValue(null, 'installed') as Integer
          yumPackage.sizeArchive = reader.getAttributeValue(null, 'archive') as Integer
        }
        else if (reader.localName == 'location') {
          yumPackage.location = reader.getAttributeValue(null, 'href')
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'header-range') {
          yumPackage.rpmHeaderStart = reader.getAttributeValue(null, 'start') as Integer
          yumPackage.rpmHeaderEnd = reader.getAttributeValue(null, 'end') as Integer
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'provides') {
          yumPackage.provides = pco = []
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'requires') {
          yumPackage.requires = pco = []
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'conflicts') {
          yumPackage.conflicts = pco = []
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'obsoletes') {
          yumPackage.obsoletes = pco = []
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'entry') {
          if (pco != null) {
            pco << new YumPackage.Entry(
                name: reader.getAttributeValue(null, 'name'),
                flags: reader.getAttributeValue(null, 'flags'),
                epoch: reader.getAttributeValue(null, 'epoch'),
                version: reader.getAttributeValue(null, 'ver'),
                release: reader.getAttributeValue(null, 'rel'),
                pre: reader.getAttributeValue(null, 'pre') == '1'
            )
          }
        }
        else if (reader.localName == 'file') {
          fileType = reader.getAttributeValue(null, 'type')
          if (!fileType) {
            fileType = 'file'
          }
        }
      }
      else if (event == XMLStreamConstants.END_ELEMENT) {
        if (reader.localName == 'package') {
          if (!writtenPrimary.contains(yumPackage.uniqueId)) {
            writePrimary(yumPackage)
            writtenPrimary.add(yumPackage.uniqueId)
          }
        }
        else if (reader.localName == 'name') {
          yumPackage.name = text
        }
        else if (reader.localName == 'arch') {
          yumPackage.arch = text
        }
        else if (reader.localName == 'checksum') {
          yumPackage.checksum = text
          yumPackage.pkgId = yumPackage.checksum
        }
        else if (reader.localName == 'summary') {
          yumPackage.summary = text
        }
        else if (reader.localName == 'description') {
          yumPackage.description = text
        }
        else if (reader.localName == 'packager') {
          yumPackage.packager = text
        }
        else if (reader.localName == 'url') {
          yumPackage.url = text
        }
        else if (reader.localName == 'summary') {
          yumPackage.summary = text
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'license') {
          yumPackage.rpmLicense = text
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'vendor') {
          yumPackage.rpmVendor = text
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'group') {
          yumPackage.rpmGroup = text
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'buildhost') {
          yumPackage.rpmBuildHost = text
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'sourcerpm') {
          yumPackage.rpmSourceRpm = text
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'provides') {
          pco = null
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'requires') {
          pco = null
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'conflicts') {
          pco = null
        }
        else if (reader.prefix == 'rpm' && reader.localName == 'obsoletes') {
          pco = null
        }
        else if (reader.localName == 'file') {
          if (!yumPackage.files) {
            yumPackage.files = []
          }
          yumPackage.files << new YumPackage.File(
              name: text,
              type: YumPackage.FileType.valueOf(fileType),
              primary: true
          )
        }
        text = null
      }
      else if (reader.isCharacters() && !reader.isWhiteSpace()) {
        text = (text ? text : '') + reader.text
      }
    }
  }

  /**
   * Read & merge package metadata from filelists.xml.
   */
  def readFiles(final XMLStreamReader reader) {
    def yumPackage = null, text = null, fileType = null
    while (reader.hasNext()) {
      int event = reader.next()
      if (event == XMLStreamConstants.START_ELEMENT) {
        if (reader.localName == 'package') {
          yumPackage = new YumPackage()
          yumPackage.pkgId = reader.getAttributeValue(null, 'pkgid')
          yumPackage.name = reader.getAttributeValue(null, 'name')
          yumPackage.arch = reader.getAttributeValue(null, 'arch')
        }
        else if (reader.localName == 'version') {
          yumPackage.epoch = reader.getAttributeValue(null, 'epoch')
          yumPackage.version = reader.getAttributeValue(null, 'ver')
          yumPackage.release = reader.getAttributeValue(null, 'rel')
        }
        else if (reader.localName == 'file') {
          fileType = reader.getAttributeValue(null, 'type')
          if (!fileType) {
            fileType = 'file'
          }
        }
      }
      else if (event == XMLStreamConstants.END_ELEMENT) {
        if (reader.localName == 'package') {
          if (!writtenFiles.contains(yumPackage.uniqueId)) {
            writeFileLists(yumPackage)
            writtenFiles.add(yumPackage.uniqueId)
          }
        }
        else if (reader.localName == 'file') {
          if (!yumPackage.files) {
            yumPackage.files = []
          }
          yumPackage.files << new YumPackage.File(
              name: text,
              type: YumPackage.FileType.valueOf(fileType),
              primary: true
          )
        }
        text = null
      }
      else if (reader.isCharacters() && !reader.isWhiteSpace()) {
        text = (text ? text : '') + reader.text
      }
    }
  }

  /**
   * Read & merge package metadata from other.xml.
   */
  def readOther(final XMLStreamReader reader) {
    def yumPackage = null, text = null, changeLog = null
    while (reader.hasNext()) {
      int event = reader.next()
      if (event == XMLStreamConstants.START_ELEMENT) {
        if (reader.localName == 'package') {
          yumPackage = new YumPackage()
          yumPackage.pkgId = reader.getAttributeValue(null, 'pkgid')
          yumPackage.name = reader.getAttributeValue(null, 'name')
          yumPackage.arch = reader.getAttributeValue(null, 'arch')
        }
        else if (reader.localName == 'version') {
          yumPackage.epoch = reader.getAttributeValue(null, 'epoch')
          yumPackage.version = reader.getAttributeValue(null, 'ver')
          yumPackage.release = reader.getAttributeValue(null, 'rel')
        }
        else if (reader.localName == 'changelog') {
          changeLog = new YumPackage.ChangeLog(
              author: reader.getAttributeValue(null, 'author'),
              date: reader.getAttributeValue(null, 'date') as Integer
          )
        }
      }
      else if (event == XMLStreamConstants.END_ELEMENT) {
        if (reader.localName == 'package') {
          if (!writtenFiles.contains(yumPackage.uniqueId)) {
            writeOther(yumPackage)
            writtenFiles.add(yumPackage.uniqueId)
          }
        }
        else if (reader.localName == 'changelog') {
          if (!yumPackage.changes) {
            yumPackage.changes = []
          }
          changeLog.text = text
          yumPackage.changes << changeLog
        }
        text = null
      }
      else if (reader.isCharacters() && !reader.isWhiteSpace()) {
        text = (text ? text : '') + reader.text
      }
    }
  }

}
