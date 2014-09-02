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
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

//
// FIXME: This class is used in many static context in tests, and can fail to locate the "baseTest" bundle.
// FIXME: When run from IDE integration this file may not have fully substituted values either.
// FIXME: Use of this class in a static context should be avoided.
//

public class TestProperties
{
  private static final Logger log = LoggerFactory.getLogger(TestProperties.class);

  private static ResourceBundle bundle;

  private static synchronized ResourceBundle getBundle() {
    if (bundle == null) {
      // HACK: log the source of the test properties
      URL url = TestProperties.class.getClassLoader().getResource("baseTest.properties");
      log.info("Loading test properties: {}", url);

      bundle = ResourceBundle.getBundle("baseTest");
      checkState(bundle != null, "Failed to load test properties from bundle: baseTest");
      log.info("Test properties:");
      List<String> keys = Lists.newArrayList(bundle.keySet());
      Collections.sort(keys);
      for (String key : keys) {
        log.info("  {}={}", key, bundle.getString(key));
      }
    }
    return bundle;
  }

  public static String getString(String key) {
    return getBundle().getString(key);
  }

  public static Integer getInteger(String key) {
    String value = getBundle().getString(key);
    return new Integer(value);
  }

  public static Map<String, String> getAll() {
    Map<String, String> properties = new LinkedHashMap<String, String>();
    Enumeration<String> keys = getBundle().getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      properties.put(key, getBundle().getString(key));
    }
    return properties;
  }

  public static File getFile(String key) {
    return new File(getString(key));
  }
}
