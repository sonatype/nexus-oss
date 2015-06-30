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
package com.sonatype.nexus.repository.nuget.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The unrendered result of a feed query.
 *
 * @since 3.0
 */
public class FeedResult
{
  private final String base;

  private final String operation;

  private final Map<String, String> query;

  private Integer count;

  private Map<String, ?> skipLinkEntry;

  private List<Map<String, ?>> entries = new ArrayList<>();

  public FeedResult(final String base, final String operation, final Map<String, String> query) {
    this.base = base;
    this.operation = operation;
    this.query = query;
  }

  public void addEntry(Map<String, ?> entry) {
    entries.add(entry);
  }

  public void setCount(final int count) {
    this.count = count;
  }

  public String getBase() {
    return base;
  }

  public String getOperation() {
    return operation;
  }

  public Map<String, String> getQuery() {
    return query;
  }

  public Integer getCount() {
    return count;
  }

  public List<Map<String, ?>> getEntries() {
    return entries;
  }

  public Map<String, ?> getSkipLinkEntry() {
    return skipLinkEntry;
  }

  public void setSkipLinkEntry(final Map<String, ?> skipLinkEntry) {
    this.skipLinkEntry = skipLinkEntry;
  }
}
