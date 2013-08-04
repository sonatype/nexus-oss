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

package org.sonatype.nexus.util;

import java.util.Collection;
import java.util.Map;

public class ContextUtils
{
  public static boolean isFlagTrue(Map<String, Object> context, String key) {
    if (context != null) {
      if (context.containsKey(key)) {
        return Boolean.TRUE.equals(context.get(key));
      }
    }

    return false;
  }

  public static void setFlag(Map<String, Object> context, String key, boolean value) {
    if (context != null) {
      if (value) {
        context.put(key, Boolean.TRUE);
      }
      else {
        context.remove(key);
      }
    }
  }

  public static boolean collContains(Map<String, Object> context, String key, Object value) {
    if (context != null && context.containsKey(key)) {
      if (context.get(key) instanceof Collection) {
        Collection<?> coll = (Collection<?>) context.get(key);

        return coll.contains(value);
      }
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  public static boolean collAdd(Map<String, Object> context, String key, Object value) {
    if (context != null && context.containsKey(key)) {
      if (context.get(key) instanceof Collection) {
        Collection<Object> coll = (Collection<Object>) context.get(key);

        return coll.add(value);
      }
    }

    return false;
  }

}
