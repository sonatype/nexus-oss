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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.adapter.ComponentEntityAdapter;
import org.sonatype.nexus.component.services.adapter.EntityAdapterSupport;
import org.sonatype.nexus.component.services.model.TestComponent;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Entity adapter for {@link TestComponent}.
 */
public class TestComponentEntityAdapter
    extends EntityAdapterSupport
    implements ComponentEntityAdapter<TestComponent>
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
  public Class<TestComponent> getComponentClass() {
    return TestComponent.class;
  }

  @Override
  public void registerStorageClass(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    if (!schema.existsClass(ORIENT_CLASS_NAME)) {
      OClass baseClass = schema.getClass(ORIENT_BASE_CLASS_NAME);
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
      logCreatedClassInfo(oClass);
    }
  }

  @Override
  public void convertToDocument(final TestComponent component, final ODocument document) {
    ComponentId id = component.getId();

    if (id == null) {
      document.field(P_ID, (Object) null);
    }
    else {
      document.field(P_ID, id.asUniqueString());
    }

    document.field(P_BINARY, component.getBinaryProp());
    document.field(P_BOOLEAN, component.getBooleanProp());
    document.field(P_BYTE, component.getByteProp());

    if (component.getDatetimeProp() == null) {
      document.field(P_DATETIME, (Object) null);
    }
    else {
      document.field(P_DATETIME, component.getDatetimeProp().toDate());
    }

    document.field(P_DOUBLE, component.getDoubleProp());
    document.field(P_EMBEDDEDLIST, component.getEmbeddedListProp());
    document.field(P_EMBEDDEDMAP, component.getEmbeddedMapProp());
    document.field(P_EMBEDDEDSET, component.getEmbeddedSetProp());
    document.field(P_FLOAT, component.getFloatProp());
    document.field(P_INTEGER, component.getIntegerProp());
    document.field(P_LONG, component.getLongProp());
    document.field(P_SHORT, component.getShortProp());
    document.field(P_STRING, component.getStringProp());
    document.field(P_UNREGISTERED, component.getUnregisteredProp());
  }

  @Override
  public TestComponent convertToComponent(final ODocument document) {
    TestComponent component = new TestComponent();

    final String id = document.field(P_ID);
    if (id != null) {
      component.setId(new ComponentId() { // TODO: Switch to use ComponentId class when available
        @Override
        public String asUniqueString() {
          return id;
        }
      });
    }

    component.setBinaryProp((byte[]) document.field(P_BINARY));
    component.setBooleanProp((Boolean) document.field(P_BOOLEAN));
    component.setByteProp((Byte) document.field(P_BYTE));

    Date date = document.field(P_DATETIME);
    if (date != null) {
      component.setDatetimeProp(new DateTime(date));
    }

    component.setDoubleProp((Double) document.field(P_DOUBLE));
    component.setEmbeddedListProp((List<String>) document.field(P_EMBEDDEDLIST));
    component.setEmbeddedMapProp((Map<String, String>) document.field(P_EMBEDDEDMAP));
    component.setEmbeddedSetProp((Set<String>) document.field(P_EMBEDDEDSET));

    component.setFloatProp((Float) document.field(P_FLOAT));
    component.setIntegerProp((Integer) document.field(P_INTEGER));
    component.setLongProp((Long) document.field(P_LONG));
    component.setShortProp((Short) document.field(P_SHORT));
    component.setStringProp((String) document.field(P_STRING));
    component.setUnregisteredProp(document.field(P_UNREGISTERED));

    return component;
  }
}
