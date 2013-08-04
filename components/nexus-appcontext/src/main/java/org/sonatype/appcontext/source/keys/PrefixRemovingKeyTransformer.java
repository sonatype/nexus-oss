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

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.source.EntrySourceMarker;
import org.sonatype.appcontext.source.WrappingEntrySourceMarker;

/**
 * A key transformer that removes a given prefix from key.
 *
 * @author cstamas
 */
public class PrefixRemovingKeyTransformer
    implements KeyTransformer
{
  private final String prefix;

  public PrefixRemovingKeyTransformer(final String prefix) {
    this.prefix = Preconditions.checkNotNull(prefix);
  }

  public EntrySourceMarker getTransformedEntrySourceMarker(final EntrySourceMarker source) {
    return new WrappingEntrySourceMarker(source)
    {
      @Override
      protected String getDescription(final EntrySourceMarker wrapped) {
        return String.format("prefixRemove(prefix:%s, %s)", prefix, wrapped.getDescription());
      }
    };
  }

  public String transform(final String key) {
    if (key.startsWith(prefix)) {
      // remove prefix, but watch for capitalization
      final String result = key.substring(prefix.length());
      if (Character.isUpperCase(result.charAt(0))) {
        final char[] resultArray = result.toCharArray();
        resultArray[0] = Character.toLowerCase(resultArray[0]);
        return new String(resultArray);
      }
      else {
        return result;
      }
    }
    else {
      return key;
    }
  }
}
