/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.quartz.internal.guice;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.quartz.QuartzPlugin;

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

/**
 * Quartz guice module setting up Kazuki.
 *
 * @since 3.0
 */
@Named
public class QuartzModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    // Kazuki
    install(new LifecycleModule(QuartzPlugin.STORE_NAME));
    bind(JdbiDataSourceConfiguration.class).annotatedWith(Names.named(QuartzPlugin.STORE_NAME))
        .toProvider(JdbiConfigurationProvider.class).in(Scopes.SINGLETON);
    final EasyKeyValueStoreModule keyValueStoreModule = new EasyKeyValueStoreModule(QuartzPlugin.STORE_NAME, null);
    keyValueStoreModule.withSequenceConfig(getSequenceServiceConfiguration());
    keyValueStoreModule.withKeyValueStoreConfig(getKeyValueStoreConfiguration());
    install(keyValueStoreModule);
  }

  // Kazuki stuff

  private SequenceServiceConfiguration getSequenceServiceConfiguration() {
    final SequenceServiceConfiguration.Builder builder = new SequenceServiceConfiguration.Builder();
    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName("quartz");
    builder.withStrictTypeCreation(true);
    return builder.build();
  }

  private KeyValueStoreConfiguration getKeyValueStoreConfiguration() {
    final KeyValueStoreConfiguration.Builder builder = new KeyValueStoreConfiguration.Builder();
    builder.withDbType("h2");
    builder.withGroupName("nexus");
    builder.withStoreName("quartz");
    builder.withPartitionName("default");
    builder.withPartitionSize(100_000L);
    builder.withStrictTypeCreation(true);
    return builder.build();
  }

  private static class JdbiConfigurationProvider
      implements Provider<JdbiDataSourceConfiguration>
  {
    private final ApplicationDirectories directories;

    @Inject
    public JdbiConfigurationProvider(final ApplicationDirectories directories) {
      this.directories = checkNotNull(directories);
    }

    @Override
    public JdbiDataSourceConfiguration get() {
      JdbiDataSourceConfiguration.Builder builder = new JdbiDataSourceConfiguration.Builder();

      builder.withJdbcDriver("org.h2.Driver");

      File dir = directories.getWorkDirectory("db/quartz");
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
