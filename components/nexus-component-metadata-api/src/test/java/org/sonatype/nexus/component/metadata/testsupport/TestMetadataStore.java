package org.sonatype.nexus.component.metadata.testsupport;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.metadata.api.AbstractMetadataStore;
import org.sonatype.nexus.component.metadata.api.MetadataHandle;
import org.sonatype.nexus.component.metadata.api.MetadataIndex;
import org.sonatype.nexus.component.metadata.api.MetadataKeyIterable;
import org.sonatype.nexus.component.metadata.api.MetadataTypeRegistry;

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
