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

package org.sonatype.appcontext.source.keys;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.appcontext.AppContextException;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySource;
import org.sonatype.appcontext.source.EntrySourceMarker;

/**
 * EntrySource that wraps another EntrySource and applies KeyTransformer to it.
 *
 * @author cstamas
 */
public class KeyTransformingEntrySource
    implements EntrySource
{
  public static final KeyTransformer NOOP = new NoopKeyTransformer();

  private final EntrySource source;

  private final KeyTransformer keyTransformer;

  private final EntrySourceMarker sourceMarker;

  public KeyTransformingEntrySource(final EntrySource source, final KeyTransformer keyTransformer) {
    this.source = Preconditions.checkNotNull(source);

    this.keyTransformer = Preconditions.checkNotNull(keyTransformer);

    this.sourceMarker = keyTransformer.getTransformedEntrySourceMarker(source.getEntrySourceMarker());
  }

  public EntrySourceMarker getEntrySourceMarker() {
    return sourceMarker;
  }

  public Map<String, Object> getEntries(AppContextRequest request)
      throws AppContextException
  {
    final Map<String, Object> result = new HashMap<String, Object>();

    for (Map.Entry<String, Object> entry : source.getEntries(request).entrySet()) {
      result.put(keyTransformer.transform(entry.getKey()), entry.getValue());
    }

    return result;
  }

}
