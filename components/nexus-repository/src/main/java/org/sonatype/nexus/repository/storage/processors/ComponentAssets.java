package org.sonatype.nexus.repository.storage.processors;

import java.util.List;

import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ProcessorContext;
import org.sonatype.nexus.repository.storage.StorageTx;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * {@link Component}'s {@link Asset} entiy source that uses {@link StorageTx#browseAssets(Component)} method. Requires
 * {@link Component} in context.
 *
 * @since 3.0
 */
public class ComponentAssets
    extends ValueElementSource<Asset>
{
  public ComponentAssets() {
    super(Asset.class);
  }

  @Override
  public List<Asset> elements(final ProcessorContext context) {
    final Component component = context.getAttributes().require(Component.class);
    final List<Asset> assets = Lists.newArrayList();
    try (StorageTx tx = context.getStorageTxSupplier().get()) {
      Iterables.addAll(
          assets,
          tx.browseAssets(component)
      );
    }
    return assets;
  }
}
