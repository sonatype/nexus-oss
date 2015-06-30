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
package org.sonatype.nexus.internal.security.model;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.nexus.security.config.CPrivilege;
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
  public static final String DB_CLASS = new OClassNameBuilder()
      .prefix("security")
      .type("privilege")
      .build();

  public static final String P_ID = "id";

  public static final String P_NAME = "name";

  public static final String P_DESCRIPTION = "description";

  public static final String P_TYPE = "type";

  public static final String P_PROPERTIES = "properties";

  private static final String I_ID = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_ID)
      .build();

  /**
   * Register schema.
   */
  public OClass register(final ODatabaseDocumentTx db, final Runnable initializer) {
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

      type.createIndex(I_ID, INDEX_TYPE.UNIQUE, P_ID);

      log.info("Created schema: {}, properties: {}", type, type.properties());

      initializer.run();
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
    document.field(P_PROPERTIES, entity.getProperties());

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public CPrivilege read(final ODocument document) {
    checkNotNull(document);

    CPrivilege entity = new CPrivilege();
    entity.setId(document.<String>field(P_ID, OType.STRING));
    entity.setName(document.<String>field(P_NAME, OType.STRING));
    entity.setDescription(document.<String>field(P_DESCRIPTION, OType.STRING));
    entity.setType(document.<String>field(P_TYPE, OType.STRING));
    entity.setReadOnly(false);
    entity.setProperties(Maps.newHashMap(document.<Map<String, String>>field(P_PROPERTIES, OType.EMBEDDEDMAP)));

    entity.setVersion(String.valueOf(document.getVersion()));

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
   * Retrieves a privilege document.
   *
   * @return found document, null otherwise
   */
  @Nullable
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

  /**
   * Deletes a privilege.
   *
   * @return true if privilege was deleted
   */
  public boolean delete(final ODatabaseDocumentTx db, final String id) {
    OCommandSQL command = new OCommandSQL(
        "DELETE FROM " + DB_CLASS + " WHERE " + P_ID + " = ?"
    );
    int records = db.command(command).execute(id);
    return records == 1;
  }
}
