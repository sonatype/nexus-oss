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

import org.sonatype.nexus.testsuite.siestajs.SiestaTestSupport;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamParameterized;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;

/**
 * Functional test-support.
 */
@RunWith(PaxExamParameterized.class)
@ExamReactorStrategy(PerClass.class)
public class FunctionalTestSupport
    extends SiestaTestSupport
{
  public FunctionalTestSupport(final String executable, final String[] options) {
    super(executable, options, "oss");
  }

  //
  // Configuration
  //

  @Configuration
  public static Option[] config() {
    return options(
        // TODO: Can we use the it.nexus.bundle.groupId and it.nexus.bundle.artifactId properties?
        nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),

        // Add testsuite dependencies as bundles
        wrappedBundle(maven("org.sonatype.nexus", "nexus-siestajs-testsupport").versionAsInProject()),
        wrappedBundle(maven("org.apache.ant", "ant-launcher").versionAsInProject()),
        wrappedBundle(maven("org.apache.ant", "ant").versionAsInProject()),

        // FIXME: This does not take effect unless you do *NOT* have NEXUS_RESOURCE_DIRS set
        // FIXME: Needs to include more resource-dirs as well to pick up changes
        editConfigurationFileExtend("etc/system.properties",
            "nexus.resource.dirs", resolveBaseFile("src/test/ft-resources").getAbsolutePath()
        )
    );
  }
}
