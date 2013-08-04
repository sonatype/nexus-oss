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

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * Represents an entry from {@link AppContext}. You usually do not want to tamper with these, as {@link AppContext}
 * exposes entry values directly over it's map-like interface.
 *
 * @author cstamas
 */
public interface AppContextEntry
{
  /**
   * Returns the creation timestamp in millis of this entry.
   *
   * @return millisecond timestamp when this entry was created.
   */
  long getCreated();

  /**
   * Returns the key this entry is keyed with.
   *
   * @return the key of entry.
   */
  String getKey();

  /**
   * Returns the value this entry holds. In case of string type, it will be interpolated.
   *
   * @return the value of entry, interpolated if value type is string.
   */
  Object getValue();

  /**
   * Returns the "raw" value of the entry. It might differ from {@link #getValue()} in case of string types values,
   * as
   * this will return uninterpolated value.
   *
   * @return the raw value of entry, uninterpolated if value is string.
   */
  Object getRawValue();

  /**
   * Returns the marker denoting from where this entry came from.
   *
   * @return the marker denoting the origin of this entry.
   */
  EntrySourceMarker getEntrySourceMarker();
}
