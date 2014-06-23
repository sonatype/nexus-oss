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
package org.sonatype.nexus.componentmetadata.testsupport;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.componentmetadata.api.AbstractMetadataStore;
import org.sonatype.nexus.componentmetadata.api.MetadataHandle;
import org.sonatype.nexus.componentmetadata.api.MetadataIndex;
import org.sonatype.nexus.componentmetadata.api.MetadataKeyIterable;
import org.sonatype.nexus.componentmetadata.api.MetadataTypeRegistry;

import com.google.common.collect.Lists;

public class TestMetadataStore<T>
    extends AbstractMetadataStore<T>
{
  private Map<String, Object> lastFilter;

  private MetadataIndex lastIndex;

  public TestMetadataStore(MetadataTypeRegistry registry, Class<T> clazz) {
    super(registry, clazz);
  }

  public Map<String, Object> getLastFilter() {
    return lastFilter;
  }

  public MetadataIndex getLastIndex() {
    return lastIndex;
  }

  @Override
  protected MetadataKeyIterable find(final Map<String, Object> filter, final MetadataIndex index) {
    lastFilter = filter;
    lastIndex = index;
    return new MetadataKeyIterable()
    {
      @Override
      public void close() throws Exception { }

      @Override
      public Iterator<String> iterator() {
        return Lists.newArrayList("first", "second").iterator();
      }
    };
  }

  @Override
  public MetadataHandle<T> create(final T metadata) {
    return null;
  }

  @Nullable
  @Override
  public MetadataHandle<T> retrieve(final String key) {
    return null;
  }

  @Override
  public MetadataHandle<T> update(final String key, @Nullable final String version, final T metadata) {
    return null;
  }

  @Override
  public boolean delete(final String key, @Nullable final String version) {
    return false;
  }
}
