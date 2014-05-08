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

package org.sonatype.nexus.quartz.internal.store;

import io.kazuki.v0.store.schema.model.Schema;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.schema.TypeValidation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple helper that holds triplet of KZ Schema name, related Java class and KZ Schema.
 *
 * @since 3.0
 */
public class TypedSchema<T>
{
  private final String name;

  private final TypeValidation typeValidation;

  private final Class<T> clazz;

  private final Schema schema;

  public TypedSchema(final String name,
                     final TypeValidation typeValidation,
                     final Class<T> clazz,
                     final Schema schema)
  {
    this.name = checkNotNull(name);
    this.typeValidation = checkNotNull(typeValidation);
    this.clazz = checkNotNull(clazz);
    this.schema = checkNotNull(schema);
  }

  public String getName() {
    return name;
  }

  public TypeValidation getTypeValidation() { return typeValidation; }

  public Class<T> getClazz() {
    return clazz;
  }

  public Schema getSchema() {
    return schema;
  }

  public void mayCreateSchema(final SchemaStore schemaStore) throws KazukiException {
    if (schemaStore.retrieveSchema(getName()) == null) {
      schemaStore.createSchema(getName(), getSchema());
    }
  }
}
