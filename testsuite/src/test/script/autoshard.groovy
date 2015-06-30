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
import org.apache.maven.project.MavenProject
import org.slf4j.Logger
import support.TestSourcesScanner

// expose types for default variables
def project = project as MavenProject
def log = log as Logger

// requirements:
// automatically shard into # of configurations (normal testsuite + scheduled testsuite)
// automatically shard into 1 configuration (custom)
// expose includes/excludes
// allow specific tests to be collected in specific shard

/**
 * Helper to read a project property, optionally with a default, and require a non-null/empty value.
 */
def property = { String name, String _default = null ->
  def value = project.properties[name]
  if (!value) {
    value = _default
  }
  if (!value) {
    fail("Missing property: $name")
  }
  return value
}

def mode = property('autoshard.mode')
def sourceDir = property('autoshard.sourceDir', project.build.testSourceDirectory) as File
def outputDir = property('autoshard.outputDir') as File
def includes = property('autoshard.includes', '**/*IT.class')
def excludes = property('autoshard.excludes', 'defaults')

def scanner = new TestSourcesScanner(
    basedir: sourceDir,
    includes: includes,
    excludes: excludes
)

/**
 * Convert a source ref (.java or .groovy, etc) to a .class ref.
 * This is needed so that surefire can properly execute compiled java and groovy sources.
 */
def sourceToClass = { filename ->
  def i = filename.lastIndexOf('.')
  def prefix = filename.substring(0, i)
  return "${prefix}.class"
}

/**
 * Mode 'custom', generates a single shard configuration.
 *
 * Special handling for empty selection to keep failsafe configuration happy; creates configuration with dummy source file.
 */
def customMode = {
  def shardId = property('autoshard.shardId', 'custom')
  def outputFile = new File(outputDir, "shard-${shardId}.txt")

  if (!sourceDir.exists()) {
    log.warn("Missing source directory: $sourceDir; creating dummy configuration: $outputFile")
    outputFile.parentFile.mkdirs()
    outputFile.text = 'WARNING_missing_source_directory.class'
    return
  }

  scanner.scan()

  if (scanner.empty) {
    log.warn("No test-sources matched include pattern; creating dummy configuration: $outputFile")
    outputFile.parentFile.mkdirs()
    outputFile.text = 'WARNING_no_test_sources_matched.class'
    return
  }

  def iter = scanner.iterator()
  def tests = []
  while (iter.hasNext()) {
    String classref = iter.next()
    classref = sourceToClass(classref)
    println "    $classref"
    tests << classref
  }

  println "Creating shard configuration: $outputFile"
  outputFile.parentFile.mkdirs()
  outputFile.withPrintWriter { writer ->
    tests.each {
      writer.println "$it"
    }
  }
}

/**
 * Mode 'normal', generates a number of shard configurations.
 */
def normalMode = {
  def count = property('autoshard.count') as int

  if (!sourceDir) {
    log.warn("Missing source directory: $sourceDir; skipping")
    return
  }

  scanner.scan()

  if (scanner.empty) {
    fail('No test-sources matched include pattern')
  }

  def iter = scanner.iterator()
  def running = true
  def shards = [:]

  // fill each shard up, 1 source at a time to pseudo-balance
  while (running) {
    for (i in 0..<count) {
      if (!iter.hasNext()) {
        running = false
      }
      else {
        // append since source to shard, auto-create source lists
        def sources = shards[i]
        if (!sources) {
          shards[i] = sources = []
        }
        sources << iter.next()
      }
    }
  }

  // write out each shard configuration
  println "Creating $count shard configurations in: $outputDir"
  outputDir.mkdirs()
  shards.each { shardId, List sources ->
    println "Shard [$shardId] size=${sources.size()}"
    def file = new File(outputDir, "shard-${shardId}.txt")
    file.withPrintWriter { writer ->
      sources.each {
        classref = sourceToClass(it)
        println "    $classref"
        writer.println "$classref"
      }
    }
  }
}

/**
 * Mode 'dummy', generates number of shard configurations which have a dummy test.
 */
def dummyMode = {
  def count = property('autoshard.count') as int
  def testName = property('autoshard.dummytest', 'DummyIT.java') as String

  println "Creating $count dummy shards with test: $testName in: $outputDir"
  outputDir.mkdirs()
  for (i in 0..<count) {
    def file = new File(outputDir, "shard-${i}.txt")
    file.text = sourceToClass(testName)
  }
}

println "Mode: $mode"

switch (mode) {
  case 'custom':
    customMode()
    break

  case 'normal':
    normalMode()
    break

  case 'dummy':
    dummyMode()
    break

  default:
    fail("Unknown mode: $mode")
}
