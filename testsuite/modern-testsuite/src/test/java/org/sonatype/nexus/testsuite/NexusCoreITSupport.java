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
package org.sonatype.nexus.testsuite;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;

import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

/**
 * Support for Nexus integration tests.
 *
 * @since 2.11.1
 */
public abstract class NexusCoreITSupport
    extends NexusPaxExamSupport
{
  /**
   * Configure Nexus with out-of-the box settings (no HTTPS).
   */
  @Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"));
  }

  /**
   * Make sure Nexus is responding on the standard base URL before continuing
   */
  @Before
  public void waitForNexus() throws Exception {
    waitFor(responseFrom(nexusUrl));
  }
}
