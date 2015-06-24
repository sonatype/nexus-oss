/*
 * Copyright (c) 2008-2015 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite;

import org.junit.Test;

/**
 * Security tests.
 */
public class SecurityIT
    extends SiestaTestSupport
{
  public SecurityIT(final String executable, final String[] options) {
    super(executable, options);
  }

  @Test
  public void suite() throws Exception {
    run("security/.*\\.t.js");
  }
}
