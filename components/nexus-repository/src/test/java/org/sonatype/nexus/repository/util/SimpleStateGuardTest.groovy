/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.util

import org.junit.Before
import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static org.junit.Assert.fail
import static org.sonatype.nexus.repository.util.SimpleStateGuardTest.State.INITIALISED
import static org.sonatype.nexus.repository.util.SimpleStateGuardTest.State.NEW
import static org.sonatype.nexus.repository.util.SimpleStateGuardTest.State.STARTED

/**
 * Tests for {@link SimpleStateGuard}.
 */
class SimpleStateGuardTest
    extends TestSupport
{
  static enum State
  {
    NEW,
    INITIALISED,
    STARTED,
    STOPPED,
    DESTROYED
  }

  private static class TriggeredAction
    implements SimpleStateGuard.Action
  {
    boolean triggered = false

    @Override
    void run() throws Exception {
      triggered = true
    }
  }

  private SimpleStateGuard<State> underTest

  @Before
  void setUp() {
    underTest = new SimpleStateGuard.Builder<State>()
        .initial(NEW)
        .create();

    assert underTest.current == NEW
  }

  @Test
  void 'basic transition'() {
    def action = new TriggeredAction()
    underTest.transition(INITIALISED)
        .from(NEW)
        .run(action)

    assert underTest.current == INITIALISED
    assert action.triggered
  }

  @Test
  void 'invalid transition'() {
    def action = new TriggeredAction()
    try {
      underTest.transition(STARTED)
          .from(INITIALISED)
          .run(action)

      fail()
    }
    catch (IllegalStateException e) {
      // expected
    }

    assert underTest.current == NEW
    assert !action.triggered
  }

  @Test
  void 'basic guard'() {
    def action = new TriggeredAction()
    underTest.guard(NEW)
        .run(action)

    assert underTest.current == NEW
    assert action.triggered
  }

  @Test
  void 'invalid guard'() {
    def action = new TriggeredAction()

    try {
      underTest.guard(INITIALISED)
          .run(action)

      fail()
    }
    catch (IllegalStateException e) {
      // expected
    }

    assert underTest.current == NEW
    assert !action.triggered
  }
}
