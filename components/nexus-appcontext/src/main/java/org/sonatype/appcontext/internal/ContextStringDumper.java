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

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextEntry;

public class ContextStringDumper
{
  public static final String dumpToString(final AppContext context) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Application context \"" + context.getId() + "\" dump:\n");
    if (context.getParent() != null) {
      sb.append("Parent context is \"" + context.getParent().getId() + "\"\n");
    }
    for (String key : context.keySet()) {
      final AppContextEntry entry = context.getAppContextEntry(key);
      sb.append(entry.toString()).append("\n");
    }
    sb.append(String.format("Total of %s entries.\n", context.size()));
    return sb.toString();
  }
}
