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

package org.sonatype.appcontext.source.filter;

import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * A filter for entries.
 *
 * @author cstamas
 */
public interface EntryFilter
{
  /**
   * Returns the filtered entry source marker.
   */
  EntrySourceMarker getFilteredEntrySourceMarker(EntrySourceMarker source);

  /**
   * Returns true if the key and entry is acceptable by this filter, otherwise false.
   *
   * @return true to accept or false to filter out the passed in key-value.
   */
  boolean accept(String key, Object entry);
}
