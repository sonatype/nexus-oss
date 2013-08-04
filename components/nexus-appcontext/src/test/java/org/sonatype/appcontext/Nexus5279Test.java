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

import java.util.Properties;

import org.sonatype.appcontext.source.PropertiesEntrySource;

import junit.framework.TestCase;

public class Nexus5279Test
    extends TestCase
{
  public void testNexus5279() {
    // create parent using two sources
    final AppContextRequest parentRequest = Factory.getDefaultRequest("parent", null);
    parentRequest.getPublishers().clear();
    final Properties p1 = new Properties();
    p1.put("foo", "bar");
    parentRequest.getSources().add(new PropertiesEntrySource("parent", p1));
    final Properties p2 = new Properties();
    p2.put("oof", "rab");
    parentRequest.getSources().add(new PropertiesEntrySource("parent-test", p2));
    final AppContext parentContext = Factory.create(parentRequest);

    // check it's properties
    assertEquals(2, parentContext.entrySet().size());
    assertEquals(2, parentContext.values().size());
    assertEquals("bar", parentContext.get("foo"));
    assertEquals("rab", parentContext.get("oof"));

    // create empty child of the parent context
    final AppContextRequest childRequest = Factory.getDefaultRequest("child", parentContext);
    childRequest.getPublishers().clear();
    final AppContext context2 = Factory.create(childRequest);

    // check child properties, key mappings are accessing (they come from parent)
    assertEquals("bar", context2.get("foo"));
    assertEquals("rab", context2.get("oof"));
    // while child is actually empty
    assertEquals(2, context2.entrySet().size());
    assertEquals(2, context2.values().size());
    // while flattened map of child has proper sizes
    assertEquals(2, context2.flatten().entrySet().size());
    assertEquals(2, context2.flatten().values().size());
  }
}
