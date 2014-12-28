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
package org.sonatype.nexus.repository.config

import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static ConfigurationAttributes.GRANDPARENT_SEPARATOR
import static org.junit.Assert.fail

/**
 * Tests for {@link ConfigurationAttributes}.
 */
class ConfigurationAttributesTest
  extends TestSupport
{
  @Test
  void 'parentKey null when no parent'() {
    ConfigurationAttributes child = new ConfigurationAttributes('foo', [:])
    assert child.key == 'foo'
    assert child.parentKey == null
  }

  @Test
  void 'parentKey includes grandparent'() {
    ConfigurationAttributes grandparent = new ConfigurationAttributes('foo', [:])
    ConfigurationAttributes parent = grandparent.child('bar')
    ConfigurationAttributes child = parent.child('baz')

    assert grandparent.key == 'foo'
    assert parent.key == 'bar'
    assert child.key == 'baz'
    assert "foo${GRANDPARENT_SEPARATOR}bar" == child.parentKey
  }

  @Test
  void 'set with map value fails'() {
    ConfigurationAttributes attributes = new ConfigurationAttributes('foo', [:])
    try {
      attributes.set('invalid', [:])
      fail()
    }
    catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  void 'child() with non-map value fails'() {
    ConfigurationAttributes attributes = new ConfigurationAttributes('foo', [:])
    attributes.set('value', false)

    try {
      attributes.child('value')
      fail()
    }
    catch (IllegalStateException e) {
      // expected
    }
  }
}
