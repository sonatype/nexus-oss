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

import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileOperations;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.kazuki.KazukiBlobMetadataStore;
import org.sonatype.nexus.blobstore.id.BlobIdFactory;
import org.sonatype.nexus.blobstore.id.UuidBlobIdFactory;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import io.kazuki.v0.store.easy.EasyKeyValueStoreModule;
import io.kazuki.v0.store.jdbi.JdbiDataSourceConfiguration;
import io.kazuki.v0.store.keyvalue.KeyValueStoreConfiguration;
import io.kazuki.v0.store.lifecycle.LifecycleModule;
import io.kazuki.v0.store.sequence.SequenceServiceConfiguration;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.blobstore.file.kazuki.KazukiBlobMetadataStore.METADATA_TYPE;

/**
 * File blob store Guice module.
 *
 * @since 3.0
 */
@Named
public class FileBlobStoreModule
    extends AbstractModule
{
  public static final String FILE_BLOB_STORE = "fileblobstore";

  @Override
  protected void configure() {
    bind(BlobMetadataStore.class).to(KazukiBlobMetadataStore.class).in(Scopes.SINGLETON);
    bind(FileOperations.class).to(SimpleFileOperations.class).in(Scopes.SINGLETON);
    bind(BlobIdFactory.class).to(UuidBlobIdFactory.class).in(Scopes.SINGLETON);

    bind(JdbiDataSourceConfiguration.class).annotatedWith(Names.named(FILE_BLOB_STORE))
        .toProvider(JdbiConfigurationProvider.class).in(Scopes.SINGLETON);

    // Kazuki lifecycle management
    install(new LifecycleModule(FILE_BLOB_STORE));

    // Kazuki key-value store
    install(new EasyKeyValueStoreModule(FILE_BLOB_STORE, null)
            .withSequenceConfig(getSequenceServiceConfiguration())
            .withKeyValueStoreConfig(getKeyValueStoreConfiguration())
    );
  }

  private SequenceServiceConfiguration getSequenceServiceConfiguration() {
    SequenceServiceConfiguration.Builder builder = new SequenceServiceConfiguration.Builder();

    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName(FILE_BLOB_STORE);
    builder.withStrictTypeCreation(true);

    return builder.build();
  }

  private KeyValueStoreConfiguration getKeyValueStoreConfiguration() {
    KeyValueStoreConfiguration.Builder builder = new KeyValueStoreConfiguration.Builder();

    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName(FILE_BLOB_STORE);
    builder.withPartitionName("default");
    builder.withPartitionSize(100_000L); // TODO: Confirm this is sensible
    builder.withStrictTypeCreation(true);
    builder.withDataType(METADATA_TYPE);
    builder.withSecondaryIndex(true);

    return builder.build();
  }

  // TODO: Extract helper for jdbi config, as the location for databases will be normalized

  private static class KazukiBlobMetadataStoreProvider
      implements Provider<KazukiBlobMetadataStore>
  {
    private String blobStoreName;

    private KazukiBlobMetadataStoreProvider(final String blobStoreName) {
      this.blobStoreName = checkNotNull(blobStoreName, "blob store name");
    }

    @Override
    public KazukiBlobMetadataStore get() {
      return null;
    }
  }

  private static class JdbiConfigurationProvider
      implements Provider<JdbiDataSourceConfiguration>
  {
    private ApplicationDirectories directories;

    @Inject
    private JdbiConfigurationProvider(final ApplicationDirectories directories) {
      this.directories = checkNotNull(directories);
    }

    @Override
    public JdbiDataSourceConfiguration get() {
      JdbiDataSourceConfiguration.Builder builder = new JdbiDataSourceConfiguration.Builder();

      builder.withJdbcDriver("org.h2.Driver");

      File dir = directories.getWorkDirectory("db/" + FILE_BLOB_STORE);
      File file = new File(dir, dir.getName());
      builder.withJdbcUrl("jdbc:h2:" + file.getAbsolutePath());

      builder.withJdbcUser("root");
      builder.withJdbcPassword("not_really_used");
      builder.withPoolMinConnections(25);
      builder.withPoolMaxConnections(25);

      return builder.build();
    }
  }
}
