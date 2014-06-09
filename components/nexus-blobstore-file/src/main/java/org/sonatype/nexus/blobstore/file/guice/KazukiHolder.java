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
  private final Lifecycle lifecycle;

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
