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
package org.sonatype.nexus.component.services.internal.adapter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapter;
import org.sonatype.nexus.component.services.model.TestComponent;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Entity adapter for {@link TestComponent}.
 */
public class TestComponentEntityAdapter
    extends EntityAdapter<TestComponent>
{
  public static final String ORIENT_CLASS_NAME = new OClassNameBuilder().type(TestComponent.class).build();

  public static final String P_BINARY = "binaryProp";
  public static final String P_BOOLEAN = "booleanProp";
  public static final String P_BYTE = "byteProp";
  public static final String P_DATETIME = "datetimeProp";
  public static final String P_DOUBLE = "doubleProp";
  public static final String P_EMBEDDEDLIST = "embeddedlistProp";
  public static final String P_EMBEDDEDMAP = "embeddedmapProp";
  public static final String P_EMBEDDEDSET = "embeddedsetProp";
  public static final String P_FLOAT = "floatProp";
  public static final String P_INTEGER = "integerProp";
  public static final String P_LONG = "longProp";
  public static final String P_SHORT = "shortProp";
  public static final String P_STRING = "stringProp"; // required and indexed (unique)...all others are optional
  public static final String P_UNREGISTERED = "unregisteredProp"; // not a formal part of the schema...all others are

  @Override
  public Class<TestComponent> getEntityClass() {
    return TestComponent.class;
  }

  @Override
  public void registerStorageClass(final ODatabaseDocumentTx db) {
    OSchema schema = checkNotNull(db).getMetadata().getSchema();
    ComponentEntityAdapter.registerBaseClass(schema, log);
    if (!schema.existsClass(ORIENT_CLASS_NAME)) {
      OClass baseClass = schema.getClass(ComponentEntityAdapter.ORIENT_CLASS_NAME);
      OClass oClass = schema.createClass(ORIENT_CLASS_NAME, baseClass);
      createOptionalProperty(oClass, P_BINARY, OType.BINARY);
      createOptionalProperty(oClass, P_BOOLEAN, OType.BOOLEAN);
      createOptionalProperty(oClass, P_BYTE, OType.BYTE);
      createOptionalProperty(oClass, P_DATETIME, OType.DATETIME);
      createOptionalProperty(oClass, P_DOUBLE, OType.DOUBLE);
      createOptionalProperty(oClass, P_EMBEDDEDLIST, OType.EMBEDDEDLIST);
      createOptionalProperty(oClass, P_EMBEDDEDMAP, OType.EMBEDDEDMAP);
      createOptionalProperty(oClass, P_EMBEDDEDSET, OType.EMBEDDEDSET);
      createOptionalProperty(oClass, P_FLOAT, OType.FLOAT);
      createOptionalProperty(oClass, P_INTEGER, OType.INTEGER);
      createOptionalProperty(oClass, P_LONG, OType.LONG);
      createOptionalProperty(oClass, P_SHORT, OType.SHORT);
      createRequiredAutoIndexedProperty(oClass, P_STRING, OType.STRING, true);
      // NOTE: P_UNREGISTERED is not a formal part of the schema, but may
      // still be used because this oClass doesn't specify strict mode
      logCreatedClassInfo(log, oClass);
    }
  }

  @Override
  public void convertToDocument(final TestComponent entity, final ODocument document) {
    ComponentEntityAdapter.convertBasePropertiesToDocument(entity, document);
    setValueOrNull(document, P_BINARY, entity.getBinaryProp());
    setValueOrNull(document, P_BOOLEAN, entity.getBooleanProp());
    setValueOrNull(document, P_BYTE, entity.getByteProp());
    setValueOrNull(document, P_DATETIME, entity.getDatetimeProp());
    setValueOrNull(document, P_DOUBLE, entity.getDoubleProp());
    setValueOrNull(document, P_EMBEDDEDLIST, entity.getEmbeddedListProp());
    setValueOrNull(document, P_EMBEDDEDMAP, entity.getEmbeddedMapProp());
    setValueOrNull(document, P_EMBEDDEDSET, entity.getEmbeddedSetProp());
    setValueOrNull(document, P_FLOAT, entity.getFloatProp());
    setValueOrNull(document, P_INTEGER, entity.getIntegerProp());
    setValueOrNull(document, P_LONG, entity.getLongProp());
    setValueOrNull(document, P_SHORT, entity.getShortProp());
    setValueOrNull(document, P_STRING, entity.getStringProp());
    setValueOrNull(document, P_UNREGISTERED, entity.getUnregisteredProp());
  }

  @Override
  public TestComponent convertToEntity(final ODocument document) {
    TestComponent entity = new TestComponent();
    ComponentEntityAdapter.convertBasePropertiesToEntity(document, entity);
    entity.setBinaryProp((byte[]) document.field(P_BINARY));
    entity.setBooleanProp((Boolean) document.field(P_BOOLEAN));
    entity.setByteProp((Byte) document.field(P_BYTE));
    entity.setDatetimeProp(getDateTimeOrNull(document, P_DATETIME));
    entity.setDoubleProp((Double) document.field(P_DOUBLE));
    entity.setEmbeddedListProp((List<String>) document.field(P_EMBEDDEDLIST));
    entity.setEmbeddedMapProp((Map<String, String>) document.field(P_EMBEDDEDMAP));
    entity.setEmbeddedSetProp((Set<String>) document.field(P_EMBEDDEDSET));
    entity.setFloatProp((Float) document.field(P_FLOAT));
    entity.setIntegerProp((Integer) document.field(P_INTEGER));
    entity.setLongProp((Long) document.field(P_LONG));
    entity.setShortProp((Short) document.field(P_SHORT));
    entity.setStringProp((String) document.field(P_STRING));
    entity.setUnregisteredProp(document.field(P_UNREGISTERED));
    return entity;
  }
}
