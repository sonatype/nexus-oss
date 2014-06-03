package org.sonatype.nexus.blobstore.file.guice;

import io.kazuki.v0.store.index.SecondaryIndexStore;
import io.kazuki.v0.store.keyvalue.KeyValueStore;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;

/**
 * Holds the various pieces of a Kazuki key value store, so they can be provided as a single object.
 *
 * @since 3.0
 */
public class KazukiHolder
{
  private  final Lifecycle lifecycle;

  private final KeyValueStore kvStore;

  private final SchemaStore schemaStore;

  private final SecondaryIndexStore secondaryIndexStore;

  public KazukiHolder(final Lifecycle lifecycle, final KeyValueStore kvStore, final SchemaStore schemaStore,
                      final SecondaryIndexStore secondaryIndexStore)
  {
    this.lifecycle = lifecycle;
    this.kvStore = kvStore;
    this.schemaStore = schemaStore;
    this.secondaryIndexStore = secondaryIndexStore;
  }

  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  public KeyValueStore getKvStore() {
    return kvStore;
  }

  public SchemaStore getSchemaStore() {
    return schemaStore;
  }

  public SecondaryIndexStore getSecondaryIndexStore() {
    return secondaryIndexStore;
  }
}
