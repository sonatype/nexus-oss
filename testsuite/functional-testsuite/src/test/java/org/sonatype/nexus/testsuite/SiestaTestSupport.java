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
package org.sonatype.nexus.testsuite;

import java.util.List;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;

import com.google.common.collect.ImmutableList;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamParameterized;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;

/**
 * Support for Siesta tests.
 */
@RunWith(PaxExamParameterized.class)
@ExamReactorStrategy(PerClass.class)
public class SiestaTestSupport
    extends NexusPaxExamSupport
{
  @Configuration
  public static Option[] config() {
    return options(
        // TODO: Can we use the it.nexus.bundle.groupId and it.nexus.bundle.artifactId properties?
        nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),

        // install Ant dependencies as bundles (required by TestLauncher.groovy)
        wrappedBundle(maven("org.apache.ant", "ant-launcher").versionAsInProject()),
        wrappedBundle(maven("org.apache.ant", "ant").versionAsInProject()),

        // FIXME: This does not take effect unless you do *NOT* have NEXUS_RESOURCE_DIRS set
        // FIXME: Needs to include more resource-dirs as well to pick up changes
        editConfigurationFileExtend("etc/system.properties",
            "nexus.resource.dirs", resolveBaseFile("src/test/ft-resources").getAbsolutePath()
        )
    );
  }

  private final String executable;

  private final String[] options;

  private static Object[] driver(final String executable, final String... options) {
    return new Object[]{executable, options};
  }

  @Parameterized.Parameters
  public static List<Object[]> drivers() {
    // FIXME: Expose this configuration for changing via build execution system properties
    return ImmutableList.of(
        driver("phantomjs")
    );
  }

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
}
