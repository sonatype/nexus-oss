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
package org.sonatype.nexus.security.source;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CProperty;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link CPrivilege} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class CPrivilegeEntityAdapter
    extends ComponentSupport
{
  public static final String DB_CLASS = new OClassNameBuilder().type("SecurityPrivilege").build();

  public static final String P_ID = "id";

  public static final String P_NAME = "name";

  public static final String P_DESCRIPTION = "description";

  public static final String P_TYPE = "type";

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

      type.createProperty(P_ID, OType.STRING).setNotNull(true);
      type.createProperty(P_NAME, OType.STRING).setNotNull(true);
      type.createProperty(P_DESCRIPTION, OType.STRING);
      type.createProperty(P_TYPE, OType.STRING).setNotNull(true);
      type.createProperty(P_PROPERTIES, OType.EMBEDDEDMAP).setNotNull(true);

      type.createIndex(DB_CLASS + "_" + P_ID + "idx", INDEX_TYPE.UNIQUE, P_ID);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  public ODocument create(final ODatabaseDocumentTx db, final CPrivilege entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Write entity to document.
   */
  public ODocument write(final ODocument document, final CPrivilege entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_ID, entity.getId());
    document.field(P_NAME, entity.getName());
    document.field(P_DESCRIPTION, entity.getDescription());
    document.field(P_TYPE, entity.getType());

    Map<String, String> properties = Maps.newHashMap();
    if (entity.getProperties() != null) {
      for (CProperty property : entity.getProperties()) {
        properties.put(property.getKey(), property.getValue());
      }
    }
    document.field(P_PROPERTIES, properties);

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public CPrivilege read(final ODocument document) {
    checkNotNull(document);

    String id = document.field(P_ID, OType.STRING);
    String name = document.field(P_NAME, OType.STRING);
    String description = document.field(P_DESCRIPTION, OType.STRING);
    String type = document.field(P_TYPE, OType.STRING);
    Map<String, String> properties = document.field(P_PROPERTIES, OType.EMBEDDEDMAP);

    CPrivilege entity = new CPrivilege();
    entity.setId(id);
    entity.setName(name);
    entity.setDescription(description);
    entity.setType(type);
    entity.setReadOnly(false);
    for (Entry<String, String> entry : properties.entrySet()) {
      CProperty property = new CProperty();
      property.setKey(entry.getKey());
      property.setValue(entry.getValue());
      entity.addProperty(property);
    }

    return entity;
  }

  /**
   * Browse all documents.
   */
  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(DB_CLASS);
  }

  /**
   * Get all privileges.
   */
  public Iterable<CPrivilege> get(final ODatabaseDocumentTx db) {
    return Iterables.transform(browse(db), new Function<ODocument, CPrivilege>()
    {
      @Nullable
      @Override
      public CPrivilege apply(@Nullable final ODocument input) {
        return input == null ? null : read(input);
      }
    });
  }

  /**
   * Delete all privileges.
   */
  public void deleteAll(final ODatabaseDocumentTx db) {
    OCommandSQL command = new OCommandSQL("DELETE FROM " + DB_CLASS);
    db.command(command).execute();
  }

  public void delete(final ODatabaseDocumentTx db, final String id) {
    OCommandSQL command = new OCommandSQL(
        "DELETE FROM " + DB_CLASS + " WHERE " + P_ID + " = ?"
    );
    db.command(command).execute(id);
  }

  public ODocument get(final ODatabaseDocumentTx db, final String id) {
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
        "SELECT FROM " + DB_CLASS + " WHERE " + P_ID + " = ?"
    );
    List<ODocument> results = db.command(query).execute(id);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }


}
