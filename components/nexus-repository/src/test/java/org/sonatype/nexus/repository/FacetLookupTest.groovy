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
package org.sonatype.nexus.repository

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static org.junit.Assert.fail

/**
 * Tests for {@link FacetLookup}.
 */
class FacetLookupTest
  extends TestSupport
{
  private FacetLookup underTest

  @Before
  void setUp() {
    underTest = new FacetLookup()
  }

  static interface ExampleFacet
    extends Facet
  {
    // empty
  }

  static class MyExampleFacet
    extends FacetSupport
    implements ExampleFacet
  {
    // empty
  }

  @Test
  void 'add and get facet'() {
    def facet1 = new MyExampleFacet()
    underTest.add(facet1)

    def facet2 = underTest.get(MyExampleFacet.class)
    assert facet1 == facet2
  }

  // FIXME: These tests should pass, but need to update the impl to cope first

  @Test
  @Ignore('FIXME: impl does not yet support this')
  void 'add and get facet subtype'() {
    def facet1 = new MyExampleFacet()
    underTest.add(facet1)

    def facet2 = underTest.get(ExampleFacet.class)
    assert facet1 == facet2
  }

  @Test
  @Ignore('FIXME: impl does not yet support this')
  void 'add duplicate facet disallowed'() {
    underTest.add(new MyExampleFacet())

    try {
      underTest.add(new MyExampleFacet())
      fail()
    }
    catch (Exception e) {
      // expected
    }
  }
}
