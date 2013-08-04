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

import org.sonatype.appcontext.AppContextEntry;
import org.sonatype.appcontext.source.EntrySourceMarker;

public class AppContextEntryImpl
    implements AppContextEntry
{
  private final long created;

  private final String key;

  private final Object rawValue;

  private final Object value;

  private final EntrySourceMarker entrySourceMarker;

  public AppContextEntryImpl(final long created, final String key, final Object rawValue, final Object value,
                             final EntrySourceMarker entrySourceMarker)
  {
    this.created = created;
    this.key = Preconditions.checkNotNull(key);
    this.rawValue = rawValue;
    this.value = value;
    this.entrySourceMarker = Preconditions.checkNotNull(entrySourceMarker);
  }

  public long getCreated() {
    return created;
  }

  public String getKey() {
    return key;
  }

  public Object getRawValue() {
    return rawValue;
  }

  public Object getValue() {
    return value;
  }

  public EntrySourceMarker getEntrySourceMarker() {
    return entrySourceMarker;
  }

  // ==

  public String toString() {
    return String.format("\"%s\"=\"%s\" (raw: \"%s\", src: %s)", key, String.valueOf(value),
        String.valueOf(rawValue), entrySourceMarker.getDescription());
  }
}
