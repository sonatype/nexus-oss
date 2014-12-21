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

import com.google.common.reflect.TypeToken
import org.junit.Before
import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

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

  /**
   * How can we make repo.facet(ExampleFacet.class) and repo.facet(MyExampleFacet.class) work
   * after repo.attach(new MyExampleFacet()) ?
   */
  @Test
  void 'experiment with facet attach type handling'() {
    def typeToken = TypeToken.of(MyExampleFacet.class)
    def types = typeToken.types
    types.each { type ->
      println type
    }
  }
}
