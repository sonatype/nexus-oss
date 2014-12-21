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
package org.sonatype.nexus.repository.config;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.nexus.repository.entity.EntityId;
import org.sonatype.nexus.repository.entity.EntityMetadata;
import org.sonatype.nexus.repository.entity.EntityVersion;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.version.ORecordVersion;
import com.orientechnologies.orient.core.version.OSimpleVersion;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Orient {@link ConfigurationStore}.
 *
 * @since 3.0
 */
public class ConfigurationStoreImpl
  extends LifecycleSupport
  implements ConfigurationStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final RecordIdObfuscator recordIdObfuscator;

  private final ConfigurationEntityAdapter entityAdapter;

  private OClass entityType;

  @Inject
  public ConfigurationStoreImpl(final @Named("config") Provider<DatabaseInstance> databaseInstance,
                                final RecordIdObfuscator recordIdObfuscator,
                                final ConfigurationEntityAdapter entityAdapter)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
    this.entityAdapter = checkNotNull(entityAdapter);
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      // register schema
      entityType = entityAdapter.register(db);
    }
  }

  @Override
  protected void doStop() throws Exception {
    entityType = null;
  }

  // TODO: Hookup to NX lifecycle

  private ODatabaseDocumentTx openDb() {
    ensureStarted();
    return databaseInstance.get().acquire();
  }

  private void attachEntityMetadata(final Configuration entity, final ODocument doc) {
    EntityId id = new EntityId(recordIdObfuscator.encode(entityType, doc.getIdentity()));
    EntityVersion version = new EntityVersion(doc.getRecordVersion().toString());
    entity.setEntityMetadata(new EntityMetadata(id, version));
  }

  private ORID decodeRecordIdentity(final EntityMetadata metadata) {
    return recordIdObfuscator.decode(entityType, metadata.getId().getValue());
  }

  private ORecordVersion decodeRecordVersion(final EntityMetadata metadata) {
    ORecordVersion version = new OSimpleVersion();
    version.getSerializer().fromString(metadata.getVersion().getValue(), version);
    return version;
  }

  @Override
  public Iterable<Configuration> browse() {
    List<Configuration> entities = Lists.newArrayList();

    try (ODatabaseDocumentTx db = openDb()) {
      for (ODocument doc : entityAdapter.browse(db)) {
        Configuration entity = entityAdapter.read(doc);
        attachEntityMetadata(entity, doc);
        entities.add(entity);
      }
    }

    return entities;
  }

  @Nullable
  @Override
  public Configuration read(final String identifier) {
    checkNotNull(identifier);

    try (ODatabaseDocumentTx db = openDb()) {
      // TODO:
    }

    return null;
  }

  @Override
  public Configuration create(final Configuration configuration) throws Exception {
    checkNotNull(configuration);

    checkState(configuration.getEntityMetadata() == null);

    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = entityAdapter.add(db, configuration);
      attachEntityMetadata(configuration, doc);
    }

    return configuration;
  }

  @Override
  public Configuration edit(final Configuration configuration) throws Exception {
    checkNotNull(configuration);

    EntityMetadata metadata = configuration.getEntityMetadata();
    checkState(metadata != null);
    ORID rid = decodeRecordIdentity(metadata);

    try (ODatabaseDocumentTx db = openDb()) {
      ODocument doc = db.getRecord(rid);
      checkState(doc != null);

      // TODO: Verify record version

      doc = entityAdapter.edit(doc, configuration);
      attachEntityMetadata(configuration, doc);
    }

    return configuration;
  }

  @Override
  public void delete(final String identifier) throws Exception {
    checkNotNull(identifier);

    try (ODatabaseDocumentTx db = openDb()) {
      // TODO:
    }
  }
}
