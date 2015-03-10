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
package org.sonatype.nexus.orient.entity;

import javax.annotation.Nullable;

import org.sonatype.nexus.common.entity.Entity;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Single record entity-adapter.
 *
 * @since 3.0
 */
public abstract class SingletonEntityAdapter<T extends Entity>
  extends EntityAdapter<T>
{
  public SingletonEntityAdapter(final String typeName) {
    super(typeName);
  }

  // TODO: Add support to verify there is only 1 entity (or none) when registering type

  /**
   * Find the singleton record, or null if not set.
   */
  @Nullable
  private ODocument find(final ODatabaseDocumentTx db) {
    Iterable<ODocument> documents = browseDocuments(db);
    if (documents.iterator().hasNext()) {
      return documents.iterator().next();
    }
    return null;
  }

  /**
   * Get singleton entity or null if not set.
   */
  @Nullable
  public T get(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    ODocument document = find(db);
    if (document != null) {
      return readEntity(document);
    }
    return null;
  }

  /**
   * Set singleton entity.
   */
  public void set(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument document = find(db);
    if (document == null) {
      addEntity(db, entity);
    }
    else {
      writeEntity(document, entity);
    }
  }

  /**
   * Remove singleton entity.
   */
  public void remove(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    ODocument document = find(db);
    if (document != null) {
      db.delete(document);
    }
  }
}
