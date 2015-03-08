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

import java.util.List;

import org.sonatype.nexus.common.entity.Entity;

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

  /**
   * Browse all entities, appending to given collection.
   */
  public List<T> browse(final ODatabaseDocumentTx db, final List<T> entities) {
    checkNotNull(entities);
    for (ODocument document : browse(db)) {
      entities.add(read(document));
    }
    return entities;
  }

  // FIXME: Cleanup support class api and signatures here, return value is never used and probably should not be exposed

  @Override
  public ODocument edit(final ODatabaseDocumentTx db, final T entity) {
    return super.edit(db, entity);
  }

  @Override
  public ODocument add(final ODatabaseDocumentTx db, final T entity) {
    return super.add(db, entity);
  }

  @Override
  public void delete(final ODatabaseDocumentTx db, final T entity) {
    super.delete(db, entity);
  }
}
