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
package org.sonatype.nexus.blobstore.file.guice;

import java.io.File;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
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
 * Encapsulates construction of a Kazuki key value store in a separate Injector to eliminate interactions with the rest
 * of Nexus' guice wiring.
 *
 * @since 3.0
 */
public class KazukiBuilder
{
  private final String name;

  private final File dbDirectory;

  public KazukiBuilder(final String name, final File dbDirectory) {
    this.name = name;
    this.dbDirectory = dbDirectory;
  }

  public KazukiHolder build() {
    final Injector injector = Guice.createInjector(new Builder(name)
        .withJdbiConfiguration(name, getJdbiDataSourceConfiguration())
        .withSequenceServiceConfiguration(name, getSequenceServiceConfiguration())
        .withKeyValueStoreConfiguration(name, getKeyValueStoreConfiguration())
        .build());

    final Lifecycle lifecycle = getInstance(Lifecycle.class, injector);
    final KeyValueStore keyValueStore = getInstance(KeyValueStore.class, injector);
    final SchemaStore schemaStore = getInstance(SchemaStore.class, injector);
    final SecondaryIndexStore secondaryIndexStore = getInstance(SecondaryIndexStore.class, injector);

    return new KazukiHolder(lifecycle, keyValueStore, schemaStore, secondaryIndexStore);
  }

  private <T> T getInstance(final Class<T> clazz, final Injector injector) {
    return injector.getInstance(Key.get(clazz, Names.named(name)));
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
