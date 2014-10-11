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

package org.sonatype.nexus.ruby;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Support class that needs heavy-weight {@link TestScriptingContainer} to have available in places like constructor.
 * The container is manager "per-class" of the test.
 */
public abstract class RubyScriptingTestSupport
    extends TestSupport
{
  protected static TestScriptingContainer testScriptingContainer;

  @BeforeClass
  public static void createContainer() {
    testScriptingContainer = new TestScriptingContainer();
  }

  @AfterClass
  public static void terminateContainer() {
    if (testScriptingContainer != null) {
      testScriptingContainer.terminate();
    }
  }
}
