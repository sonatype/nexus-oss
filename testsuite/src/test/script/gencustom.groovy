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
import org.apache.tools.ant.DirectoryScanner

def testDir = project.build.testSourceDirectory as File
def testIncludes = project.properties['testsuite.customIncludes'] as String
def shardDir = project.properties['testsuite.shardDir'] as File
def shardFile = new File(shardDir, 'shard-custom.txt')

if (!testDir.exists()) {
    log.warn('Missing test directory; skipping: {}', testDir)
    return
}

println "Scanning test-classes in: ${testDir}"

def scanner = new DirectoryScanner()
def includes = []
def excludes = []
for (pattern in testIncludes.split(',')) {
    if (pattern.startsWith('!')) {
        excludes << pattern.trim()
    }
    else {
        includes << pattern.trim()
    }
}
println "    Include: $includes"
println "    Exclude: $excludes"

scanner.basedir = testDir
scanner.includes = includes
scanner.excludes = excludes
scanner.scan()

shardFile.parentFile.mkdirs()

if (scanner.includedFilesCount == 0) {
    log.warn("No test-classes matched include pattern; creating empty file: ${shardFile}")
    shardFile.text = "# WARNING: No test-classes matched included pattern: ${testIncludes}"
    return
}

println "Found ${scanner.includedFilesCount} test-classes:"

// sorting to help get consistent order
def iter = scanner.includedFiles.toList().sort().iterator()
def tests = []
while (iter.hasNext()) {
    def file = iter.next()
    println "    $file"
    tests << file
}

println "Creating shard configuration: ${shardFile}"

shardFile.withPrintWriter { writer ->
    tests.each {
        writer.println "$it"
    }
}