/*
 * Copyright (c) 2008-2015 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
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

  public SiestaTestSupport(final String executable, final String[] options) {
    this.executable = executable;
    this.options = options;
  }

  /**
   * Run a set of tests.
   *
   * @param include Regular expression of tests to run.
   */
  protected void run(final String include) throws InterruptedException {
    waitFor(responseFrom(nexusUrl));
    TestLauncher launcher = new TestLauncher(executable, options, nexusUrl);
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
    return ImmutableList.of(driver("phantomjs"));
  }
}
