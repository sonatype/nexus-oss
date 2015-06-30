/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package support

import org.apache.tools.ant.DirectoryScanner

/**
 * Helper to scan for test .java and .groovy sources.
 *
 * Supports special handling to treat .class patterns as .java nad .groovy.
 */
class TestSourcesScanner
{
  private DirectoryScanner scanner = new DirectoryScanner()

  File basedir

  String includes

  String excludes

  void scan() {
    println "Scanning test-sources in: $basedir"
    scanner.basedir = basedir

    def includes = []
    def excludes = []

    /**
     * Helper to append a source pattern to a list.
     * If the pattern ends with .class will add items for .java and .groovy.
     */
    def append = { list, pattern ->
      if (pattern.endsWith('.class')) {
        def i = pattern.lastIndexOf('.')
        def prefix = pattern.substring(0, i)
        list << "${prefix}.java"
        list << "${prefix}.groovy"
      }
      else {
        list << pattern
      }
    }

    for (pattern in this.includes.trim().split(',')) {
      // includes can contain excludes pattern
      if (pattern.startsWith('!')) {
        append excludes, pattern.trim()
      }
      else {
        append includes, pattern.trim()
      }
    }

    for (pattern in this.excludes.trim().split(',')) {
      append excludes, pattern.trim()
    }

    // always exclude some sources
    excludes << '**/Abstract*.java'
    excludes << '**/Abstract*.groovy'

    scanner.includes = includes
    println "Includes: $includes"

    scanner.excludes = excludes
    println "Excludes: $excludes"

    scanner.scan()

    println "Found $scanner.includedFilesCount test-sources"
  }

  boolean isEmpty() {
    return scanner.includedFilesCount == 0
  }

  Iterator iterator() {
    // sorting to help get consistent order
    return scanner.includedFiles.toList().sort().iterator()
  }
}