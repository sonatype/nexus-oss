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
package org.sonatype.nexus.jmx

import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import javax.management.DescriptorKey
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Tests for {@link DescriptorHelper}
 */
class DescriptorHelperTest
    extends TestSupport
{
  @TestAuthor('jason')
  class TestBean
  {
    @TestComments('foo bar baz')
    def foo() {
      // ignore
    }
  }

  @Test
  void 'finds annotations'() {
    def bean = new TestBean()
    def annotations = DescriptorHelper.findAllAnnotations(bean.getClass().annotations)
    log annotations

    // custom annotation should be found
    assert annotations.find { it.annotationType().name == TestAuthor.class.name}
  }

  @Test
  void 'build descriptor from type'() {
    def bean = new TestBean()
    def descriptor = DescriptorHelper.build(bean.getClass())
    log descriptor

    // descriptor should have author
    assert descriptor.fields.length == 1
    assert descriptor.getFieldValue('author') == 'jason'
  }

  @Test
  void 'build descriptor from method'() {
    def method = TestBean.class.getMethod('foo')
    def descriptor = DescriptorHelper.build(method)
    log descriptor

    // descriptor should have comments
    assert descriptor.fields.length == 1
    assert descriptor.getFieldValue('comments') == 'foo bar baz'
  }
}

//
// Annotations to help verify processing
//

@Target([ElementType.TYPE, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface TestAuthor
{
  @DescriptorKey('author')
  String value()
}

@Target([ElementType.TYPE, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
@interface TestComments
{
  @DescriptorKey('comments')
  String value()
}
