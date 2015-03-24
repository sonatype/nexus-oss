package com.sonatype.nexus.repository.nuget.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The unrendered result of a feed query.
 *
 * @since 3.0
 */
class FeedResult
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
