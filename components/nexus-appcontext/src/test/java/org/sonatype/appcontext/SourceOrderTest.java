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

import java.io.File;
import java.util.ArrayList;

import org.sonatype.appcontext.source.PropertiesFileEntrySource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SourceOrderTest
{
  @Before
  public void before() {
    System.setProperty("one", "100");
    System.setProperty("order.five", "500");
  }

  @After
  public void after() {
    System.clearProperty("one");
    System.clearProperty("order.five");
  }

  @Test
  public void simpleOrder() {
    // we test the ordering
    // we have _two_ property files + we have system properties set too

    // we expect to have properties applies as:
    // system properties are used, if not found then
    // mostimportant.properties are used, if not found then
    // leastimportant.properties are used.

    // keys:
    // appcontext "ID" is "order", meaning all the "order." prefixed system properties will be "harvested"
    // appcontext has an "inclusion" defined for key "one", system properties and env variable with same name will
    // be "harvested"
    // and we have two sources coming from properties files
    final AppContextRequest req = Factory.getDefaultRequest("order", null, new ArrayList<String>(), "one");

    req.getSources().add(0,
        new PropertiesFileEntrySource(new File("src/test/resources/order/mostimportant.properties")));
    req.getSources().add(0,
        new PropertiesFileEntrySource(new File("src/test/resources/order/leastimportant.properties")));

    System.out.println(req.getSources());

    final AppContext context = Factory.create(req);

    Assert.assertEquals(5, context.size());

    // "one" -- should come from system properties (overrides all) and have value 1000
    Assert.assertEquals("100", context.get("one"));
    // "two" -- should come from "mostimportant.properties" file (overrides "leastimportant.properties") and have
    // value 20
    Assert.assertEquals("20", context.get("two"));
    // "three" -- should come from "leastimportant.properties" file and have value 3
    Assert.assertEquals("3", context.get("three"));
    // "four" -- should come from "mostimportant.properies" and have value 40
    Assert.assertEquals("40", context.get("four"));
    // "five" -- should come from system properites and have value 500
    Assert.assertEquals("500", context.get("five"));
  }

}
