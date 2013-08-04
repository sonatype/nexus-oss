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

package org.sonatype.appcontext.internal;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class HierarchicalMapTest
{
  @Test
  public void simpleFunctionalityTest() {
    final HierarchicalMap<String, String> m1 = new HierarchicalMap<String, String>();
    final HierarchicalMap<String, String> m2 = new HierarchicalMap<String, String>(m1);
    Object[] values;

    // m2 inherits from m1
    m1.put("one", "1");
    Assert.assertEquals("1", m2.get("one"));
    values = m2.flatten().values().toArray();
    Arrays.sort(values);
    Assert.assertArrayEquals(new Object[]{"1"}, values);

    // m2 overrides the m1
    m2.put("one", "one");
    Assert.assertEquals("one", m2.get("one"));
    values = m2.flatten().values().toArray();
    Arrays.sort(values);
    Assert.assertArrayEquals(new Object[]{"one"}, values);

    // m1 change not visible by m2 override
    m1.put("one", "egy");
    Assert.assertEquals("one", m2.get("one"));
    values = m2.flatten().values().toArray();
    Arrays.sort(values);
    Assert.assertArrayEquals(new Object[]{"one"}, values);

    // m2 override remove, m1 inherited
    m2.remove("one");
    Assert.assertEquals("egy", m2.get("one"));
    values = m2.flatten().values().toArray();
    Arrays.sort(values);
    Assert.assertArrayEquals(new Object[]{"egy"}, values);

    // m2 new value
    m2.put("two", "2");
    Assert.assertEquals("egy", m2.get("one"));
    values = m2.flatten().values().toArray();
    Arrays.sort(values);
    Assert.assertArrayEquals(new Object[]{"2", "egy"}, values);

    // new value moved to parent, get result same but flatten affected?
    m2.remove("two");
    m1.put("two", "2");
    Assert.assertEquals("egy", m2.get("one"));
    values = m2.flatten().values().toArray();
    Arrays.sort(values);
    Assert.assertArrayEquals(new Object[]{"2", "egy"}, values);
  }
}
