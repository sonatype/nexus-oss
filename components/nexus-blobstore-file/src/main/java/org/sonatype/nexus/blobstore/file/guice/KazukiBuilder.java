package org.sonatype.nexus.blobstore.file.guice;

import java.io.File;

import javax.inject.Provider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.kazuki.v0.store.guice.KazukiModule.Builder;
import io.kazuki.v0.store.index.SecondaryIndexStore;
import io.kazuki.v0.store.jdbi.JdbiDataSourceConfiguration;
import io.kazuki.v0.store.keyvalue.KeyValueStore;
import io.kazuki.v0.store.keyvalue.KeyValueStoreConfiguration;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.sequence.SequenceServiceConfiguration;

import static org.sonatype.nexus.blobstore.file.kazuki.KazukiBlobMetadataStore.METADATA_TYPE;

/**
 * @since 3.0
 */
public class KazukiBuilder
    implements Provider<KazukiHolder>
{
  private final String name;

  private final File dbDirectory;

  public KazukiBuilder(final String name, final File dbDirectory) {
    this.name = name;
    this.dbDirectory = dbDirectory;
  }

  @Override
  public KazukiHolder get() {
    final Injector injector = Guice.createInjector(new Builder(name)
        .withJdbiConfiguration(name, getJdbiDataSourceConfiguration())
        .withSequenceServiceConfiguration(name, getSequenceServiceConfiguration())
        .withKeyValueStoreConfiguration(name, getKeyValueStoreConfiguration())
        .build());

    final Lifecycle lifecycle = injector.getInstance(Lifecycle.class);
    final KeyValueStore keyValueStore = injector.getInstance(KeyValueStore.class);
    final SchemaStore schemaStore = injector.getInstance(SchemaStore.class);
    final SecondaryIndexStore secondaryIndexStore = injector.getInstance(SecondaryIndexStore.class);

    return new KazukiHolder(lifecycle, keyValueStore, schemaStore, secondaryIndexStore);
  }

  private JdbiDataSourceConfiguration getJdbiDataSourceConfiguration() {
    JdbiDataSourceConfiguration.Builder builder = new JdbiDataSourceConfiguration.Builder();

    builder.withJdbcDriver("org.h2.Driver");
    builder.withJdbcUrl("jdbc:h2:" + dbDirectory);
    builder.withJdbcUser("root");
    builder.withJdbcPassword("not_really_used");
    builder.withPoolMinConnections(25);
    builder.withPoolMaxConnections(25);

    return builder.build();
  }

  private SequenceServiceConfiguration getSequenceServiceConfiguration() {
    SequenceServiceConfiguration.Builder builder = new SequenceServiceConfiguration.Builder();

    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName(name);
    builder.withStrictTypeCreation(true);

    return builder.build();
  }

  private KeyValueStoreConfiguration getKeyValueStoreConfiguration() {
    KeyValueStoreConfiguration.Builder builder = new KeyValueStoreConfiguration.Builder();

    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName(name);
    builder.withPartitionName("default");
    builder.withPartitionSize(100_000L); // TODO: Confirm this is sensible
    builder.withStrictTypeCreation(true);
    builder.withDataType(METADATA_TYPE);
    builder.withSecondaryIndex(true);

    return builder.build();
  }
}
