/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.httpbridge.internal.describe;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Accumulates a renderable description of request-processing activity.
 *
 * @since 3.0
 */
public class Description
{
  final private Map<String, Object> parameters;

  final private List<DescriptionItem> items = Lists.newArrayList();

  public Description(final Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public Description topic(final String name) {
    items.add(new DescriptionItem(name, "topic", name));
    return this;
  }

  public Description add(final String name, final Object value) {
    items.add(new DescriptionItem(name, "string", String.valueOf(value)));
    return this;
  }

  public Description addTable(final String name, final Map<String, Object> values) {
    items.add(new DescriptionItem(name, "table", values));
    return this;
  }

  public Description addAll(final String name, final Iterable values) {
    int row = 0;
    Map<String, Object> table = Maps.newHashMap();
    for (Object value : values) {
      table.put("" + row++, value);
    }
    return addTable(name, table);
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public List<DescriptionItem> getItems() {
    return items;
  }
}
