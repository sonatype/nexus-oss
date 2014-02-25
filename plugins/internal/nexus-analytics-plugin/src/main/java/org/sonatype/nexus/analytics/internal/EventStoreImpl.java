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
package org.sonatype.nexus.analytics.internal;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.analytics.EventData;
import org.sonatype.nexus.analytics.EventStore;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.ImmutableList;
import io.kazuki.v0.internal.v2schema.Attribute;
import io.kazuki.v0.internal.v2schema.Attribute.Type;
import io.kazuki.v0.internal.v2schema.Schema;
import io.kazuki.v0.store.journal.JournalStore;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.schema.TypeValidation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link EventStore} implementation.
 *
 * @since 2.8
 */
@Named
@Singleton
public class EventStoreImpl
  extends LifecycleSupport
  implements EventStore
{
  private final JournalStore store;

  private final SchemaStore schemaStore;

  private final Lifecycle lifecycle;

  @Inject
  public EventStoreImpl(final @Named("nexusanalytics") JournalStore store,
                        final @Named("nexusanalytics") SchemaStore schemaStore,
                        final @Named("nexusanalytics") Lifecycle lifecycle)
  {
    this.store = checkNotNull(store);
    this.schemaStore = checkNotNull(schemaStore);
    this.lifecycle = checkNotNull(lifecycle);
  }
  
  @Override
  protected void doStart() throws Exception {
    lifecycle.init();
    lifecycle.start();
    
    if (schemaStore.retrieveSchema(SCHEMA_NAME) == null) {
      Schema schema = new Schema(ImmutableList.of(
          new Attribute("type", Type.UTF8_SMALLSTRING, null, false),
          new Attribute("timestamp", Type.I64, null, true),
          new Attribute("userId", Type.UTF8_SMALLSTRING, null, true),
          new Attribute("sessionId", Type.UTF8_SMALLSTRING, null, true),
          new Attribute("attributes", Type.MAP, null, true)));
      
      schemaStore.createSchema(SCHEMA_NAME, schema);
    }
  }

  @Override
  protected void doStop() throws Exception {
    lifecycle.stop();
    lifecycle.shutdown();
  }

  public JournalStore getJournalStore() {
    ensureStarted();
    return store;
  }

  @Override
  public void add(final EventData data) throws Exception {
    checkNotNull(data);
    ensureStarted();
    store.append(SCHEMA_NAME, EventData.class, data, TypeValidation.STRICT);
  }

  @Override
  public void clear() throws Exception {
    ensureStarted();
    store.clear();
    log.debug("Cleared");
  }

  @Override
  public long approximateSize() throws Exception {
    ensureStarted();
    return store.approximateSize();
  }

  @Override
  public KeyValueIterable<KeyValuePair<EventData>> iterator(final long offset, @Nullable Long limit) throws Exception {
    ensureStarted();
    return store.entriesRelative(SCHEMA_NAME, EventData.class, offset, limit);
  }
}
