/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.orient.entity;

import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.entity.Entity;
import org.sonatype.nexus.common.entity.EntityId;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Collection of records entity-adapter.
 *
 * @since 3.0
 */
public abstract class CollectionEntityAdapter<T extends Entity>
    extends EntityAdapter<T>
{
  public CollectionEntityAdapter(final String typeName) {
    super(typeName);
  }

  @Nullable
  public T get(final ODatabaseDocumentTx db, final EntityId id) {
    checkNotNull(id);
    return get(db, id.toString());
  }

  @Nullable
  public T get(final ODatabaseDocumentTx db, final String id) {
    checkNotNull(db);
    checkNotNull(id);
    ODocument doc = db.getRecord(getRecordIdObfuscator().decode(getType(), id));
    if (doc == null) {
      return null;
    }
    return readEntity(doc);
  }

  // TODO: Add (closable) iterator/iterable-based "browse" to avoid needing full memory copies for large collections?

  /**
   * Browse all entities, appending to given collection.
   */
  public List<T> browse(final ODatabaseDocumentTx db, final List<T> entities) {
    checkNotNull(entities);
    for (ODocument document : browseDocuments(db)) {
      entities.add(readEntity(document));
    }
    return entities;
  }

  public List<T> browse(final ODatabaseDocumentTx db) {
    // TODO: Could consider using countClass and Lists.newArrayListWithExpectedSize() ?

    return browse(db, Lists.<T>newLinkedList());
  }

  /**
   * Edit entity.
   */
  public void edit(final ODatabaseDocumentTx db, final T entity) {
    super.editEntity(db, entity);
  }

  /**
   * Add entity.
   */
  public void add(final ODatabaseDocumentTx db, final T entity) {
    super.addEntity(db, entity);
  }

  /**
   * Delete entity.
   */
  public void delete(final ODatabaseDocumentTx db, final T entity) {
    super.deleteEntity(db, entity);
  }
}
