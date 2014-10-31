/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.trials

import org.sonatype.nexus.client.core.NexusClient

/**
 * Support for "scenarios".
 */
abstract class ScenarioSupport
{
  protected final String id

  protected final File workdir

  protected final NxController nx

  private final AntBuilder ant

  private final File execLog

  private File cwd

  private String counter = '000';

  ScenarioSupport(final String id, final File workdir, final NexusClient nexusClient) {
    assert id != null
    assert workdir != null
    assert nexusClient != null
    this.id = id
    this.workdir = workdir
    this.nx = new NxController(nexusClient)
    this.ant = new AntBuilder()
    this.execLog = new File(workdir, '.log')
    assert execLog.mkdirs()
    this.cwd = workdir
  }

  /**
   * Step that configures scenario.
   */
  void configure() {
    // nop
  }

  /**
   * Main entry point for scenario. Normal return from this method is considered as "scenario succeeded", problems
   * should be signalled using exception.
   */
  abstract void perform();

  // ==

  /**
   * Executes a command using bash.
   */
  void exec(List<String> commands) {
    counter = String.format('%03d', Integer.valueOf(counter) + 1)
    // args are basically 2nd argument passed to bash
    String argLine = commands.collect { "\"${it}\"" }.join(' ')
    new File(execLog, "${counter}-cmd.txt").text = "CMD: ${argLine}\nCWD: ${cwd}"
    ant.exec(executable: 'bash', dir: cwd, output: lastOutFile, failOnError: true)
        {
          arg(value: '-lc')
          arg(value: argLine)
        }
  }

  /**
   * Returns the STDOUT file of last executed command.
   */
  File getLastOutFile() {
    return new File(execLog, "${counter}-out.txt")
  }

  /**
   * Returns the CWD.
   */
  File file() {
    return cwd;
  }

  /**
   * Returns a file with specified name from CWD. No checks performed (for existence, kind) are performed.
   */
  File file(String pathelem) {
    return new File(cwd, pathelem)
  }

  /**
   * Performs a "change directory".
   */
  void cd(String pathelem) {
    if ('..'.equals(pathelem)) {
      cwd = cwd.parentFile
    }
    else {
      File newWorkDir = new File(cwd, pathelem);
      assert newWorkDir.directory
      cwd = newWorkDir
    }
  }

  /**
   * Performas a "make directory" relative from CWD.
   */
  void mkdir(String pathelem) {
    ant.mkdir(dir: new File(cwd, pathelem))
  }
}
