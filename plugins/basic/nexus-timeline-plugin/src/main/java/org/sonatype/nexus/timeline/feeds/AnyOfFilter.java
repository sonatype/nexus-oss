/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.timeline.feeds;

import java.util.Set;

import org.sonatype.nexus.timeline.Entry;

import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Predicate for {@link Entry} filtering that implements "key" associated value is in set of. When the values set has
 * one value, it's basically reduces to "equals" predicate.
 *
 * @since 3.0
 */
public class AnyOfFilter
    implements Predicate<Entry>
{
  private final String key;

  private final Set<String> values;

  public AnyOfFilter(final String key, final Set<String> values) {
    this.key = checkNotNull(key);
    this.values = checkNotNull(values);
  }

  @Override
  public boolean apply(final Entry hit) {
    return (hit.getData().containsKey(key)
        && values.contains(hit.getData().get(key)));
  }
}
