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

package org.sonatype.appcontext.source;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;

/**
 * A static EntrySource that holds the key and value to make it into AppContext. Useful in testing, or when you need to
 * add one key=value into context, and you need to calculate those somehow before constructing AppContext.
 *
 * @author cstamas
 */
public class StaticEntrySource
    implements EntrySource, EntrySourceMarker
{
  private final String key;

  private final Object value;

  public StaticEntrySource(final String key, final Object val) {
    this.key = Preconditions.checkNotNull(key);
    this.value = val;
  }

  public String getDescription() {
    if (value != null) {
      return String.format("static(\"%s\"=\"%s\")", key, String.valueOf(value));
    }
    else {
      return String.format("static(\"%s\"=null)", key);
    }
  }

  public EntrySourceMarker getEntrySourceMarker() {
    return this;
  }

  public Map<String, Object> getEntries(AppContextRequest request)
      throws AppContextException
  {
    final Map<String, Object> result = new HashMap<String, Object>(1);
    result.put(key, value);
    return result;
  }
}
