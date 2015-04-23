package org.sonatype.nexus.repository.storage.processors;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Component entity source that uses {@link StorageTx#findComponents(String, Map, Iterable, String)} method. This
 * processor is prone to OOM if selection is not performed carefully, as no paging happens.
 *
 * @since 3.0
 */
public class Components
    extends ValueElementSource<Component>
{
  @Nullable
  private final String whereClause;

  @Nullable
  private final Map<String, Object> parameters;

  @Nullable
  private final Iterable<Repository> repositories;

  @Nullable
  private final String querySuffix;

  public Components(final @Nullable String whereClause,
                    final @Nullable Map<String, Object> parameters,
                    final @Nullable Iterable<Repository> repositories,
                    final @Nullable String querySuffix)
  {
    super(Component.class);
    this.whereClause = whereClause;
    this.parameters = parameters;
    this.repositories = repositories;
    this.querySuffix = querySuffix;
  }

  @Override
  public List<Component> elements(final ProcessorContext context) {
    final List<Component> components = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      Iterables.addAll(
          components,
          tx.findComponents(
              whereClause,
              parameters,
              repositories,
              querySuffix
          )
      );
    }
    return components;
  }
}
