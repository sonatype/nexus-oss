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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * EntryFilter that filters on key-prefix (keys are Strings) using key.startsWith() method, hence, this is case
 * sensitive! You can supply a list of key prefixes to accept.
 *
 * @author cstamas
 */
public class KeyPrefixEntryFilter
    implements EntryFilter
{
  private final List<String> prefixes;

  public KeyPrefixEntryFilter(final String... prefixes) {
    this(Arrays.asList(prefixes));
  }

  public KeyPrefixEntryFilter(final List<String> prefixes) {
    this.prefixes = Collections.unmodifiableList(Preconditions.checkNotNull(prefixes));
  }

  public boolean accept(final String key, final Object value) {
    if (key != null) {
      for (String prefix : prefixes) {
        if (key.startsWith(prefix)) {
          return true;
        }
      }
    }

    return false;
  }

  public EntrySourceMarker getFilteredEntrySourceMarker(final EntrySourceMarker source) {
    return new WrappingEntrySourceMarker(source)
    {
      @Override
      protected String getDescription(final EntrySourceMarker wrapped) {
        return String.format("filter(keyStartsWith:%s, %s)", prefixes, wrapped.getDescription());
      }
    };
  }
}