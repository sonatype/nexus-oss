/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.view;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ListMultimap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Headers container.
 *
 * @since 3.0
 */
public class Headers
{
  private final ListMultimap<String,String> backing;

  public Headers(final ListMultimap<String, String> headers) {
    this.backing = checkNotNull(headers);
  }

  public List<String> getAll(final String name) {
    return backing.get(name);
  }

  @Nullable
  public String get(final String name) {
    List<String> values = backing.get(name);
    if (!values.isEmpty()) {
      return values.get(0);
    }
    return null;
  }

  // TODO: type coercion helpers

  public Set<String> names() {
    return backing.keySet();
  }

  public int size() {
    return backing.size();
  }

  @Override
  public String toString() {
    return backing.toString();
  }
}
