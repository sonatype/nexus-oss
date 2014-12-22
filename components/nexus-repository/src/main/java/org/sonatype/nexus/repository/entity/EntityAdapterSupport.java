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
package org.sonatype.nexus.repository.entity;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Support for {@link EntityAdapter} implementations.
 *
 * @since 3.0
 */
public abstract class EntityAdapterSupport<T>
  extends ComponentSupport
  implements EntityAdapter<T>
{
  private final String className;

  public EntityAdapterSupport(final String className) {
    this.className = checkNotNull(className);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "className='" + className + '\'' +
        '}';
  }

  @Override
  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(className);
    if (type == null) {
      type = schema.createClass(className);
      defineSchema(type);

      log.info("Created schema class '{}': properties={}, indexes={}",
          type,
          type.properties(),
          type.getIndexes()
      );
    }

    return type;
  }

  protected abstract void defineSchema(final OClass type);

  @Override
  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(className);
  }

  @Override
  public ODocument add(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);
    ODocument doc = db.newInstance(className);
    return edit(doc, entity);
  }
}
