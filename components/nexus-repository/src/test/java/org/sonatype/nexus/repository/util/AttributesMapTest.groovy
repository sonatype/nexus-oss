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
package org.sonatype.nexus.repository.util

import org.junit.Before
import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static org.junit.Assert.fail

/**
 * Tests for {@link AttributesMap}.
 */
class AttributesMapTest
  extends TestSupport
{
  private AttributesMap underTest

  @Before
  void setUp() {
    underTest = new AttributesMap()
  }

  @Test
  void 'required value'() {
    underTest.set('foo', 'bar')
    def value = underTest.require('foo')
    assert value == 'bar'

    try {
      underTest.require('baz')
      fail()
    }
    catch (Exception e) {
      // expected
    }
  }

  @Test
  void 'defined value'() {
    underTest.set('foo', 'bar')
    assert underTest.defined('foo')
    assert !underTest.defined('baz')
  }

  @Test
  void 'size empty and clear'() {
    log underTest
    assert underTest.isEmpty()
    assert underTest.size() == 0

    underTest.set('foo', 'bar')
    log underTest
    assert underTest.size() == 1

    underTest.set('baz', 'ick')
    log underTest
    assert underTest.size() == 2

    underTest.clear()
    log underTest
    assert underTest.isEmpty()
    assert underTest.size() == 0
  }
}
