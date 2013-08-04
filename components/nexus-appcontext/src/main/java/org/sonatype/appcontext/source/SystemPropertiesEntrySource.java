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
import java.util.Properties;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;

/**
 * EntrySource that sources itself from System.getProperties().
 *
 * @author cstamas
 */
public class SystemPropertiesEntrySource
    implements EntrySource, EntrySourceMarker
{
  public String getDescription() {
    return "system(properties)";
  }

  public EntrySourceMarker getEntrySourceMarker() {
    return this;
  }

  public Map<String, Object> getEntries(AppContextRequest request)
      throws AppContextException
  {
    final Properties sysprops = System.getProperties();
    final Map<String, Object> result = new HashMap<String, Object>();
    for (Map.Entry<Object, Object> entry : sysprops.entrySet()) {
      result.put(String.valueOf(entry.getKey()), entry.getValue());
    }
    return result;
  }
}
