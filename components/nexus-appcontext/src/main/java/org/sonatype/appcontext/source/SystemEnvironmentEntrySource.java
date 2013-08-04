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

/**
 * EntrySource that sources itself from {@code System.getenv()} call. It also may perform "normalization" of the keys,
 * as:
 * <ul>
 * <li>makes all keys lower case</li>
 * <li>replaces all occurrences of character '_' (underscore) to '.' (dot)</li>
 * </ul>
 * This is needed to make it possible to have different sources have same keys.
 *
 * @author cstamas
 */
public class SystemEnvironmentEntrySource
    implements EntrySource, EntrySourceMarker
{
  public String getDescription() {
    return "system(env)";
  }

  public EntrySourceMarker getEntrySourceMarker() {
    return this;
  }

  public Map<String, Object> getEntries(AppContextRequest request)
      throws AppContextException
  {
    final Map<String, String> envMap = System.getenv();
    final Map<String, Object> result = new HashMap<String, Object>();
    for (Map.Entry<String, String> entry : envMap.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
}
