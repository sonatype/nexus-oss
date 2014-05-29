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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.FileOperations;
import org.sonatype.nexus.blobstore.file.FilePathPolicy;
import org.sonatype.nexus.blobstore.file.HashingSubdirFileLocationPolicy;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.kazuki.KazukiBlobMetadataStore;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import io.kazuki.v0.store.guice.KazukiModule;
import io.kazuki.v0.store.index.SecondaryIndexStore;
import io.kazuki.v0.store.jdbi.JdbiDataSourceConfiguration;
import io.kazuki.v0.store.keyvalue.KeyValueStore;
import io.kazuki.v0.store.keyvalue.KeyValueStoreConfiguration;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.sequence.SequenceServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.blobstore.file.kazuki.KazukiBlobMetadataStore.METADATA_TYPE;

/**
 * A Guice module for creating filesystem-based {@link BlobStore} instances. The name provided to the constructor must
 * be unique across an instance of Nexus, and is used to locate the Kazuki database and the blob store's content
 * directory.
 *
 * This private module exposes two beans:
 *
 * <ul>
 * <li>a {@link BlobStore} {@code @Named(<name constructor parameter>)} </li>
 * <li>a {@link Lifecycle} {@code @Named(<name constructor parameter>)} </li>
 * </ul>
 *
 * @since 3.0
 */
public class FileBlobStoreModule
    extends PrivateModule
{
  private final String name;

  private static final String KZ_DB_LOCATION = "fileblobstore-kazukidb-location";

  private final Logger log = LoggerFactory.getLogger(FileBlobStoreModule.class);

  /**
   * The {@code name} parameter must be unique across all of Nexus.
   */
  @Inject
  public FileBlobStoreModule(final String name) {
    this.name = checkNotNull(name, "name");
  }

  @Override
  protected void configure() {
    //binder().requireExplicitBindings();

    // Create and expose blob store itself
    bind(String.class).annotatedWith(Names.named(FileBlobStore.FILE_BLOB_STORE_NAME_BINDING)).toInstance(name);
    bind(BlobStore.class).annotatedWith(Names.named(name)).to(FileBlobStore.class);
    bind(FileOperations.class).to(SimpleFileOperations.class).in(Scopes.SINGLETON);
    expose(BlobStore.class).annotatedWith(Names.named(name));

    // Create the metadata store and expose its Lifecycle
    bind(BlobMetadataStore.class).to(KazukiBlobMetadataStore.class).in(
        Scopes.SINGLETON);
    bind(Lifecycle.class).annotatedWith(Names.named(name))
        .to(KazukiBlobMetadataStore.class);
    expose(Lifecycle.class).annotatedWith(Names.named(name));

    // Create the Kazuki database the metadata store needs
    bind(JdbiDataSourceConfiguration.class).annotatedWith(Names.named(name)).toProvider(
        JdbiDsConfigProvider.class).in(Scopes.SINGLETON);

    install(new KazukiModule.Builder(name)
        .withJdbiConfiguration(name, Key.get(JdbiDataSourceConfiguration.class, Names.named(name)))
        .withSequenceServiceConfiguration(name, getSequenceServiceConfiguration())
        .withKeyValueStoreConfiguration(name, getKeyValueStoreConfiguration())
        .build());

    bindKazukiBlobMetadataStoreDependency(io.kazuki.v0.store.lifecycle.Lifecycle.class);
    bindKazukiBlobMetadataStoreDependency(KeyValueStore.class);
    bindKazukiBlobMetadataStoreDependency(SchemaStore.class);
    bindKazukiBlobMetadataStoreDependency(SecondaryIndexStore.class);
  }

  /**
   * The {@link KazukiBlobMetadataStore} parameters need to be associated with the specific kazuki store's name.
   */
  @SuppressWarnings("unchecked")
  private void bindKazukiBlobMetadataStoreDependency(final Class type) {
    bind(type).annotatedWith(Names.named(KazukiBlobMetadataStore.DYNAMIC_KZ_NAME))
        .to(Key.get(type, Names.named(name)));
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

  @Provides
  FilePathPolicy provideFilePathPolicy(final ApplicationDirectories applicationDirectories) {
    final File workDirectory = applicationDirectories.getWorkDirectory("fileblobstore/" + name + "/content");
    return new HashingSubdirFileLocationPolicy(workDirectory.toPath());
  }

  @Provides
  @Named(KZ_DB_LOCATION)
  File provideKazukiDatabaseLocation(final ApplicationDirectories applicationDirectories) {
    File dir = applicationDirectories.getWorkDirectory("fileblobstore/" + name + "/db");
    log.info("File blob store {} metadata stored in Kazuki db: {}", name, dir);
    File file = new File(dir, dir.getName());
    return file.getAbsoluteFile();
  }

  /**
   * Using a provider class here is necessary so that the provider can be bound to an arbitrary {@code @Name}.
   */
  static class JdbiDsConfigProvider
      implements Provider<JdbiDataSourceConfiguration>
  {
    private final File kazukiDatabaseLocation;

    @Inject
    JdbiDsConfigProvider(@Named(KZ_DB_LOCATION) final File kazukiDatabaseLocation) {
      this.kazukiDatabaseLocation = kazukiDatabaseLocation;
    }

    @Override
    public JdbiDataSourceConfiguration get() {
      JdbiDataSourceConfiguration.Builder builder = new JdbiDataSourceConfiguration.Builder();

      builder.withJdbcDriver("org.h2.Driver");
      builder.withJdbcUrl("jdbc:h2:" + kazukiDatabaseLocation);
      builder.withJdbcUser("root");
      builder.withJdbcPassword("not_really_used");
      builder.withPoolMinConnections(25);
      builder.withPoolMaxConnections(25);

      return builder.build();
    }
  }
}
