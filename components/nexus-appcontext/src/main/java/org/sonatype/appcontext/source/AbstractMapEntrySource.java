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
 * An EntrySource that is sourced from a {@code java.util.Map}.
 *
 * @author cstamas
 */
public abstract class AbstractMapEntrySource
    implements EntrySource, EntrySourceMarker
{
  private final String name;

  private final String type;

  public AbstractMapEntrySource(final String name, final String type) {
    this.name = Preconditions.checkNotNull(name);
    this.type = Preconditions.checkNotNull(type);
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getDescription() {
    return String.format("%s(%s)", getType(), getName());
  }

  public final EntrySourceMarker getEntrySourceMarker() {
    return this;
  }

  public Map<String, Object> getEntries(AppContextRequest request)
      throws AppContextException
  {
    final Map<String, Object> result = new HashMap<String, Object>();

    for (Map.Entry<?, ?> entry : getSource().entrySet()) {
      if (entry.getValue() != null) {
        result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
      }
      else {
        result.put(String.valueOf(entry.getKey()), null);
      }
    }

    return result;
  }

  // ==

  protected abstract Map<?, ?> getSource();
}
