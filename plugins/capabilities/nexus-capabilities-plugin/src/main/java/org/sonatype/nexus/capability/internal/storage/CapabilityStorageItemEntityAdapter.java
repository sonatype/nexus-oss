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
package org.sonatype.nexus.capability.internal.storage;

import java.util.Map;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link CapabilityStorageItem} entity adapter.
 *
 * @since 3.0
 */
public class CapabilityStorageItemEntityAdapter
  extends ComponentSupport
{
  public static final String DB_PREFIX = "capability";

  public static final String DB_CLASS = new OClassNameBuilder()
      .prefix(DB_PREFIX)
      .type(CapabilityStorageItem.class)
      .build();

  public static final String P_VERSION = "version";

  public static final String P_TYPE = "type";

  public static final String P_ENABLED = "enabled";

  public static final String P_NOTES = "notes";

  public static final String P_PROPERTIES = "properties";

  /**
   * Register schema.
   */
  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);
      type.createProperty(P_VERSION, OType.INTEGER);
      type.createProperty(P_TYPE, OType.STRING);
      type.createProperty(P_ENABLED, OType.BOOLEAN);
      type.createProperty(P_NOTES, OType.STRING);
      type.createProperty(P_PROPERTIES, OType.EMBEDDEDMAP);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  public ODocument create(final ODatabaseDocumentTx db, final CapabilityStorageItem entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Write entity to document.
   */
  public ODocument write(final ODocument document, final CapabilityStorageItem entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_VERSION, entity.getVersion());
    document.field(P_TYPE, entity.getType());
    document.field(P_ENABLED, entity.isEnabled());
    document.field(P_NOTES, entity.getNotes());
    document.field(P_PROPERTIES, entity.getProperties());

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public CapabilityStorageItem read(final ODocument document) {
    checkNotNull(document);

    CapabilityStorageItem entity = new CapabilityStorageItem();
    entity.setVersion((Integer) document.field(P_VERSION, OType.INTEGER));
    entity.setType((String) document.field(P_TYPE, OType.STRING));
    entity.setEnabled((Boolean) document.field(P_ENABLED, OType.BOOLEAN));
    entity.setNotes((String) document.field(P_NOTES, OType.STRING));

    Map<String,String> properties = document.field(P_PROPERTIES, OType.EMBEDDEDMAP);
    entity.setProperties(properties);

    return entity;
  }

  /**
   * Browse all documents.
   */
  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(DB_CLASS);
  }
}
