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
def shardDir = project.properties['testsuite.shardDir'] as File
def shardCount = project.properties['testsuite.shardCount'] as int
def shards = [:]

if (!testDir.exists()) {
    log.warn('Missing test directory; skipping: {}', testDir)
    return
}

println "Scanning test-classes in ${testDir}"

def scanner = new DirectoryScanner()
scanner.basedir = testDir
scanner.includes = [ '**/*IT.java' ]
scanner.scan()

if (scanner.includedFilesCount == 0) {
    fail('No test-classes matched include pattern!')
}

println "Found ${scanner.includedFilesCount} test-classes"

// sorting to help get consistent shards regardless of scanner order
def iter = scanner.includedFiles.toList().sort().iterator()
def running = true
while (running) {
    for (i in 0 ..< shardCount) {
        if (!iter.hasNext()) {
            running = false
        }
        else {
            def list = shards[i]
            if (!list) {
                shards[i] = list = []
            }
            list << iter.next()
        }
    }
}

println "Creating ${shardCount} shard configurations in ${shardDir}"

shards.each { key, value ->
    println "Shard [$key] size=${value.size()}"
    def file = new File(shardDir, "shard-${key}.txt")
    file.parentFile.mkdirs()
    file.withPrintWriter { writer ->
        value.each {
            writer.println "$it"
        }
    }
}