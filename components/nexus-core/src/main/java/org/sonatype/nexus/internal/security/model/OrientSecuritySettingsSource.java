/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.security.model;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.nexus.security.settings.SecuritySettings;
import org.sonatype.nexus.security.settings.SecuritySettingsSource;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SecuritySettingsSource} implementation using Orient db as store.
 *
 * TODO remove EventSubscriber and replace with component lifecycle (NEXUS-7303)
 *
 * @since 3.0
 */
@Named
@Singleton
public class OrientSecuritySettingsSource
    extends LifecycleSupport
    implements SecuritySettingsSource, EventSubscriber
{
  /**
   * Configuration database.
   */
  private final Provider<DatabaseInstance> databaseInstance;

  /**
   * The defaults configuration source.
   */
  private final SecuritySettingsSource securityDefaults;

  /**
   * {@link SecuritySettings} entity adapter.
   */
  private final SecuritySettingsEntityAdapter entityAdapter;

  /**
   * The configuration.
   */
  private SecuritySettings configuration;

  @Inject
  public OrientSecuritySettingsSource(final @Named("security") Provider<DatabaseInstance> databaseInstance,
                                      final @Named("static") SecuritySettingsSource securityDefaults,
                                      final SecuritySettingsEntityAdapter entityAdapter)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.securityDefaults = checkNotNull(securityDefaults);
    this.entityAdapter = checkNotNull(entityAdapter);
  }

  @Override
  protected void doStart() {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      entityAdapter.register(db);
    }
  }

  @Override
  public SecuritySettings getConfiguration() {
    return configuration;
  }

  @Override
  public SecuritySettings loadConfiguration() {
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = get(db);
      if (doc == null) {
        saveConfiguration(securityDefaults.loadConfiguration());
        doc = get(db);
      }
      configuration = entityAdapter.read(doc);
      return getConfiguration();
    }
  }

  @Override
  public void storeConfiguration() {
    saveConfiguration(getConfiguration());
  }

  private void saveConfiguration(final SecuritySettings configuration) {
    checkNotNull(configuration, "Missing security configuration");
    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = get(db);
      if (doc == null) {
        entityAdapter.create(db, configuration);
      }
      else {
        entityAdapter.write(doc, configuration);
      }
    }
  }

  /**
   * Get the only records if exists, null otherwise.
   */
  private ODocument get(final ODatabaseDocumentTx db) {
    Iterable<ODocument> documents = entityAdapter.browse(db);
    if (documents.iterator().hasNext()) {
      return documents.iterator().next();
    }
    return null;
  }

  /**
   * Open a database connection using the pool.
   */
  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  /**
   * Start itself on nexus start.
   * TODO remove this and replace with component lifecycle (NEXUS-7303)
   */
  @Subscribe
  public void on(final NexusInitializedEvent event) throws Exception {
    start();
  }

  /**
   * Stop itself on nexus shutdown.
   * TODO remove this and replace with component lifecycle (NEXUS-7303)
   */
  @Subscribe
  public void on(final NexusStoppingEvent event) throws Exception {
    stop();
  }
}
