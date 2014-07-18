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
package org.sonatype.nexus.componentmetadata.internal;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.componentmetadata.FieldDefinition;
import org.sonatype.nexus.componentmetadata.RecordType;
import org.sonatype.nexus.componentmetadata.RecordStoreSchema;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexDefinition;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * OrientDB implementation of {@link RecordStoreSchema}.
 *
 * @since 3.0
 */
class OrientRecordStoreSchema
    implements RecordStoreSchema
{
  public static final Map<Class, OType> OTYPES = Maps.newHashMap();

  static {
    OTYPES.put(BigDecimal.class, OType.DECIMAL);
    OTYPES.put(Byte.class, OType.BYTE);
    OTYPES.put(Date.class, OType.DATETIME);
    OTYPES.put(Double.class, OType.DOUBLE);
    OTYPES.put(Float.class, OType.FLOAT);
    OTYPES.put(Integer.class, OType.INTEGER);
    OTYPES.put(Long.class, OType.LONG);
    OTYPES.put(Short.class, OType.SHORT);
    OTYPES.put(String.class, OType.STRING);
  }

  private final OrientRecordStoreSession session;

  private final OSchema oSchema;

  public OrientRecordStoreSchema(OrientRecordStoreSession session, OSchema oSchema) {
    this.session = checkNotNull(session);
    this.oSchema = checkNotNull(oSchema);
  }

  @Override
  public boolean addType(final RecordType type) {
    session.checkOpen();
    checkTypes(checkNotNull(type));

    if (!oSchema.existsClass(type.getName())) {
      OClass oClass;
      if (type.isAbstract()) {
        oClass = oSchema.createAbstractClass(type.getName());
      }
      else {
        oClass = oSchema.createClass(type.getName());
      }
      for (FieldDefinition field : type) {
        createProperty(oClass, field);
      }
      oClass.setStrictMode(type.isStrict());
      RecordType superType = type.getSuperType();
      if (superType != null) {
        OClass oSuperClass = oSchema.getClass(superType.getName());
        checkState(oSuperClass != null,
            "Supertype of %s does not exist yet: %s", type.getName(), superType.getName());
        oClass.setSuperClass(oSuperClass);
      }
      return true;
    }
    return false;
  }

  @Override
  public RecordType getType(final String name) {
    session.checkOpen();
    checkArgument(hasType(checkNotNull(name)), "No such type: %s", name);

    return getRecordType(oSchema.getClass(name));
  }

  @Override
  public boolean hasType(final String name) {
    session.checkOpen();

    return oSchema.existsClass(checkNotNull(name));
  }

  @Override
  public void dropType(final String name) {
    session.checkOpen();
    checkArgument(hasType(checkNotNull(name)), "No such type: %s", name);

    session.execute(String.format("DELETE FROM %s", name));
    try {
      oSchema.dropClass(name);
    }
    catch (OSchemaException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public Iterator<RecordType> iterator() {
    session.checkOpen();

    List<RecordType> types = Lists.newArrayList();
    for (OClass oClass : oSchema.getClasses()) {
      types.add(getRecordType(oClass));
    }
    return types.iterator();
  }

  private void createProperty(OClass oClass, FieldDefinition field) {
    OProperty oProperty = oClass.createProperty(field.getName(), OTYPES.get(field.getType()));
    if (field.isNotNull()) {
      oProperty.setNotNull(true);
      oProperty.setMandatory(true);
    }
    if (field.isIndexed()) {
      final String indexName = String.format("%s.%s", oClass.getName(), field.getName());
      oClass.createIndex(indexName, field.isUnique() ? INDEX_TYPE.UNIQUE : INDEX_TYPE.NOTUNIQUE, field.getName());
    }
  }

  private static RecordType getRecordType(OClass oClass) {
    RecordType type = new RecordType(oClass.getName());
    for (OProperty oProperty : oClass.properties()) {
      OType oType = oProperty.getType();
      FieldDefinition field = new FieldDefinition(oProperty.getName(), oType.getDefaultJavaType());
      field.withNotNull(oProperty.isNotNull());
      for (OIndex oIndex : oProperty.getAllIndexes()) {
        OIndexDefinition def = oIndex.getDefinition();
        if (def.isAutomatic() && def.getFields().size() == 1) {
          boolean unique = oIndex.getType().equalsIgnoreCase(INDEX_TYPE.UNIQUE.name());
          field.withIndexed(true).withUnique(unique);
        }
      }
      type.withField(field);
    }
    type.withStrict(oClass.isStrictMode());
    OClass oSuperClass = oClass.getSuperClass();
    if (oSuperClass != null) {
      type.withSuperType(getRecordType(oSuperClass));
    }
    type.withAbstract(oClass.isAbstract());
    return type;
  }

  private static void checkTypes(RecordType type) {
    for (FieldDefinition field : type) {
      checkArgument(OTYPES.containsKey(field.getType()), "Unsupported field type: %s", field.getType());
    }
  }
}
