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
package org.sonatype.nexus.timeline.internal;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.timeline.TimelinePlugin;
import org.sonatype.nexus.timeline.internal.EntryRecord;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import io.kazuki.v0.store.guice.KazukiModule;
import io.kazuki.v0.store.jdbi.JdbiDataSourceConfiguration;
import io.kazuki.v0.store.keyvalue.KeyValueStoreConfiguration;
import io.kazuki.v0.store.sequence.SequenceServiceConfiguration;

/**
 * Timeline Guice module.
 *
 * @since 3.0
 */
@Named
public class TimelineModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    bind(JdbiDataSourceConfiguration.class).annotatedWith(Names.named(TimelinePlugin.ARTIFACT_ID))
        .toProvider(JdbiConfigurationProvider.class).in(Scopes.SINGLETON);

    install(new KazukiModule.Builder(TimelinePlugin.ARTIFACT_ID)
        .withJdbiConfiguration(TimelinePlugin.ARTIFACT_ID, Key.get(JdbiDataSourceConfiguration.class, Names.named(TimelinePlugin.ARTIFACT_ID)))
        .withSequenceServiceConfiguration(TimelinePlugin.ARTIFACT_ID, getSequenceServiceConfiguration())
        .withJournalStoreConfiguration(TimelinePlugin.ARTIFACT_ID, getKeyValueStoreConfiguration())
        .build());
  }

  private SequenceServiceConfiguration getSequenceServiceConfiguration() {
    SequenceServiceConfiguration.Builder builder = new SequenceServiceConfiguration.Builder();

    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName("timeline");
    builder.withStrictTypeCreation(true);

    return builder.build();
  }

  private KeyValueStoreConfiguration getKeyValueStoreConfiguration() {
    KeyValueStoreConfiguration.Builder builder = new KeyValueStoreConfiguration.Builder();

    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName("timeline");
    builder.withPartitionName("default");
    builder.withPartitionSize(100_000L);
    builder.withStrictTypeCreation(true);
    builder.withDataType(EntryRecord.SCHEMA_NAME); // Journal store supports one schema only and this is must

    return builder.build();
  }

  private static class JdbiConfigurationProvider
      implements Provider<JdbiDataSourceConfiguration>
  {
    private final ApplicationDirectories config;

    @Inject
    public JdbiConfigurationProvider(ApplicationDirectories config) {
      this.config = config;
    }

    @Override
    public JdbiDataSourceConfiguration get() {
      JdbiDataSourceConfiguration.Builder builder = new JdbiDataSourceConfiguration.Builder();

      builder.withJdbcDriver("org.h2.Driver");

      File basedir = config.getWorkDirectory("db/timeline");
      File file = new File(basedir, basedir.getName());
      builder.withJdbcUrl("jdbc:h2:" + file.getAbsolutePath());

      builder.withJdbcUser("root");
      builder.withJdbcPassword("not_really_used");
      builder.withPoolMinConnections(25);
      builder.withPoolMaxConnections(25);

      return builder.build();
    }
  }
}
