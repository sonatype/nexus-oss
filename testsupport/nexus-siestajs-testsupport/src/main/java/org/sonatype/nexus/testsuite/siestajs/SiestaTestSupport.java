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
package org.sonatype.nexus.testsuite.siestajs;

import java.util.List;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;

import com.google.common.collect.ImmutableList;
import org.junit.runners.Parameterized;

/**
 * Support for Siesta-JS based tests.
 *
 * @since 3.0
 */
public class SiestaTestSupport
    extends NexusPaxExamSupport
{
  private final String executable;

  private final String[] options;

  /**
   * The testsuite name to pass to {@code testsuite.html?name=}.
   */
  private final String testsuiteName;

  public SiestaTestSupport(final String executable,
                           final String[] options,
                           final String testsuiteName)
  {
    this.executable = executable;
    this.options = options;
    this.testsuiteName = testsuiteName;
  }

  /**
   * Run a set of tests.
   *
   * @param include Regular expression of tests to run.
   */
  protected void run(final String include) throws InterruptedException {
    waitFor(responseFrom(nexusUrl));
    TestLauncher launcher = new TestLauncher(executable, options, nexusUrl, testsuiteName);
    launcher.launch(include);
  }

  //
  // Configuration
  //

  private static Object[] driver(final String executable, final String... options) {
    return new Object[]{executable, options};
  }

  @Parameterized.Parameters
  public static List<Object[]> drivers() {
    // FIXME: Expose this configuration for changing via build execution system properties
    return ImmutableList.of(
        driver("phantomjs", "--pause=500", "--page-pause=500")
    );
  }
}
