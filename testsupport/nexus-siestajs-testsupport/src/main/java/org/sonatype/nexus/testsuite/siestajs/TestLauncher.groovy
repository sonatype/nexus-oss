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
package org.sonatype.nexus.testsuite.siestajs

import org.sonatype.sisu.litmus.testsupport.TestUtil

import org.apache.tools.ant.BuildLogger
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.condition.Os
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.junit.Assert.fail

/**
 * Siesta-JS test launcher.
 *
 * @since 3.0
 */
class TestLauncher
{
  private static final IS_WINDOWS = Os.isFamily(Os.FAMILY_WINDOWS)

  private static final Logger log = LoggerFactory.getLogger(this)

  private static final TestUtil util = new TestUtil(this)

  private final String id = "SIESTA-${System.currentTimeMillis()}"

  private File binDirectory

  private String executableName

  private List<String> executableOptions

  private URL nexusUrl

  private String testsuiteName

  boolean debug = true

  TestLauncher(final String executableName,
               final String[] executableOptions,
               final URL nexusUrl,
               final String testsuiteName)
  {
    log.info 'ID: {}', id

    this.binDirectory = util.resolveFile('src/test/ft-resources/testsuite-lib/siesta/bin').canonicalFile
    assert binDirectory.exists()
    log.info 'Binaries directory: {}', this.binDirectory

    this.executableName = executableName
    log.info 'Executable name: {}', this.executableName

    this.executableOptions = executableOptions
    log.info 'Executable options: {}', this.executableOptions

    this.nexusUrl = nexusUrl
    log.info 'Nexus URL: {}', this.nexusUrl

    this.testsuiteName = testsuiteName
    log.info 'Testsuite name: {}', this.testsuiteName
  }

  /**
   * Create the AntBuilder which will be used to execute the test launcher.
   */
  private AntBuilder createAntBuilder() {
    def ant = new AntBuilder()
    def obj = ant.project.buildListeners[0]
    if (obj instanceof BuildLogger) {
      BuildLogger logger = (BuildLogger) obj
      logger.emacsMode = true
      logger.messageOutputLevel = debug ? Project.MSG_DEBUG : Project.MSG_VERBOSE
    }
    return ant
  }

  /**
   * Resolve the executable binary.
   */
  private File getExecutable() {
    return new File(binDirectory, executableName + (IS_WINDOWS ? '.bat' : '')).canonicalFile
  }

  /**
   * Resolve the base report file.
   */
  private File getReportFile() {
    return util.resolveFile("target/failsafe-reports/${id}_.xml")
  }

  /**
   * Find the report file.
   */
  private File findReportFile() {
    def file = getReportFile()
    if (!file.exists()) {
      // else we have to try harder
      File dir = util.resolveFile('target/failsafe-reports')
      for (f in dir.listFiles()) {
        if (f.name.startsWith(id)) {
          file = f
          break
        }
      }
    }

    return file
  }

  /**
   * Resolve the testsuite url.
   */
  private URL getUrl() {
    return new URL("${nexusUrl}testsuite.html?name=${testsuiteName}")
  }

  /**
   * Launch tests.
   *
   * @param include   Regular expression of tests.
   */
  void launch(String include) {
    def ant = createAntBuilder()

    def executable = getExecutable()
    log.info 'Executable: {}', executable
    assert executable.exists()

    def url = getUrl()
    log.info 'URL: {}', url

    def reportFile = getReportFile()
    log.info 'Report file: {}', reportFile

    def printsep = {
      println '-' * 79
    }

    println()
    printsep()
    println "Test; id=$id, include=$include"
    printsep()
    println()

    ant.exec(
        executable: executable.path,
        failIfExecutionFails: true,
        failOnerror: false,
        resultProperty: 'result') {

      arg(value: url.toExternalForm())

      executableOptions?.each {
        arg(value: it)
      }

      arg(value: '--verbose')
      if (debug) {
        arg(value: '--debug')
      }

      arg(value: "--include=$include")
      arg(value: '--no-color')

      arg(value: '--report-format=JUnit')
      arg(value: "--report-file=${reportFile.path}")
    }

    printsep()
    println()

    def result = ant.project.getProperty 'result'
    assertResult result
  }

  /**
   * Assert test results if there were any failures.
   */
  private void assertResult(String result) {
    log.info 'Result: {}', result
    assert result != null
    int code = Integer.parseInt(result)

    /*
    http://bryntum.com/docs/siesta/#!/guide/siesta_automation

    Exit Codes

    0 - All tests passed successfully
    1 - Some tests failed
    2 - Inactivity timeout while running the test suite
    3 - No supported browsers available on this machine
    4 - No tests to run (probably filter doesn't match any test url)
    5 - Can't open harness page
    6 - Wrong command line arguments
    7 - Something was wrong (generic error code for selenium, indicating there was an error in one or more browsers)
    8 - Exit after showing the Siesta version (when --version is provided on the command line)
    */

    switch (code) {
      case 0:
        log.info 'All tests passed'
        break

      case 1:
        assertTestReportDetails()
        break

      case 2:
        fail 'Inactivity timeout while running the test suite'
        break

      case 3:
        fail 'No supported browsers available on this machine'
        break

      case 4:
        fail 'No tests to run (probably include filter does not match any test url)'
        break

      case 5:
        fail 'Can not open harness page'
        break

      case 6:
        fail 'Wrong command line arguments'
        break

      case 7:
        fail 'Something was wrong (generic error code for selenium, indicating there was an error in one or more browsers)'
        break

      default:
        fail "Unknown result code: $result"
    }
  }

  /**
   * Assert failure from test report details.
   */
  private void assertTestReportDetails() {
    File file = findReportFile()
    log.info 'Report file: {}', file
    assert file.exists()

    def testsuite = new XmlSlurper().parse(file)

    StringBuilder buff = new StringBuilder()

    // render error and failure counts
    buff << 'Some tests failed:'
    if (testsuite.@errors != 0) {
      buff << " errors=${testsuite.@errors}"
    }
    if (testsuite.@failures != 0) {
      buff << " failures=${testsuite.@failures}"
    }
    buff << '\n'

    // render all errors and failures
    testsuite.testcase.each { testcase ->
      def renderDetail = { type, element ->
        buff << "[${type}] ${testcase.@name} (${element.@type}): ${element.@message}\n"

        // append text detail if there is some
        String text = element.text().trim()
        if (text.size() != 0) {
          buff << "$text\n"
        }

        buff << '\n'
      }

      testcase.error.each {
        renderDetail 'ERROR', it
      }
      testcase.failure.each {
        renderDetail 'FAILURE', it
      }
    }

    fail(buff.toString().trim())
  }
}
