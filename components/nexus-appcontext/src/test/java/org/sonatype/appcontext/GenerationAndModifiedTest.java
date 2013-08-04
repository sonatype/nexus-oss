/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.appcontext;

import java.util.Collections;

import junit.framework.TestCase;
import org.junit.Assert;

public class GenerationAndModifiedTest
    extends TestCase
{
  public void testGeneration() {
    final AppContext a1 = Factory.create("a1", null, Collections.EMPTY_MAP);
    final AppContext a2 = Factory.create("a2", a1, Collections.EMPTY_MAP);
    final AppContext a3 = Factory.create("a3", a2, Collections.EMPTY_MAP);

    Assert.assertEquals(0, a1.getGeneration());
    Assert.assertEquals(0, a2.getGeneration());
    Assert.assertEquals(0, a3.getGeneration());

    // just do anything that would modify a1
    a1.clear();

    Assert.assertEquals(1, a1.getGeneration());
    Assert.assertEquals(1, a2.getGeneration());
    Assert.assertEquals(1, a3.getGeneration());

    // just do anything that would modify a2
    a2.clear();

    Assert.assertEquals(1, a1.getGeneration());
    Assert.assertEquals(2, a2.getGeneration());
    Assert.assertEquals(2, a3.getGeneration());

    // just do anything that would modify a3
    a3.clear();

    Assert.assertEquals(1, a1.getGeneration());
    Assert.assertEquals(2, a2.getGeneration());
    Assert.assertEquals(3, a3.getGeneration());

    // just do anything that would modify a1
    a1.clear();

    Assert.assertEquals(2, a1.getGeneration());
    Assert.assertEquals(3, a2.getGeneration());
    Assert.assertEquals(4, a3.getGeneration());

    // just do anything that would modify a1
    a1.clear();

    // Assert.assertEquals( 3, a1.getGeneration() );
    // Assert.assertEquals( 4, a2.getGeneration() );
    Assert.assertEquals(5, a3.getGeneration());

  }

}
