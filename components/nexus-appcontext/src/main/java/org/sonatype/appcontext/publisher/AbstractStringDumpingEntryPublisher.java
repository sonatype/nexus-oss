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

package org.sonatype.appcontext.publisher;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.internal.ContextStringDumper;

public abstract class AbstractStringDumpingEntryPublisher
    implements EntryPublisher
{
  public String getDumpAsString(final AppContext context) {
    return getDumpAsString(context, false);
  }

  public String getDumpAsString(final AppContext context, final boolean recursively) {
    final StringBuilder sb = new StringBuilder();
    if (recursively && context.getParent() != null) {
      sb.append(getDumpAsString(context.getParent(), recursively));
    }
    sb.append(ContextStringDumper.dumpToString(context));
    return sb.toString();
  }
}
