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

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.security.model.CRole;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
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
 * {@link CRole} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class CRoleEntityAdapter
    extends ComponentSupport
{
  public static final String DB_CLASS = new OClassNameBuilder().type("SecurityRole").build();

  public static final String P_ID = "id";

  public static final String P_NAME = "name";

  public static final String P_DESCRIPTION = "description";

  public static final String P_PRIVILEGES = "privileges";

  public static final String P_ROLES = "roles";

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
      type.createProperty(P_PRIVILEGES, OType.EMBEDDEDLIST);
      type.createProperty(P_ROLES, OType.EMBEDDEDLIST);

      type.createIndex(DB_CLASS + "_" + P_ID + "idx", INDEX_TYPE.UNIQUE, P_ID);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  public ODocument create(final ODatabaseDocumentTx db, final CRole entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Write entity to document.
   */
  public ODocument write(final ODocument document, final CRole entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_ID, entity.getId());
    document.field(P_NAME, entity.getName());
    document.field(P_DESCRIPTION, entity.getDescription());
    document.field(P_PRIVILEGES, entity.getPrivileges());
    document.field(P_ROLES, entity.getRoles());

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public CRole read(final ODocument document) {
    checkNotNull(document);

    String id = document.field(P_ID, OType.STRING);
    String name = document.field(P_NAME, OType.STRING);
    String description = document.field(P_DESCRIPTION, OType.STRING);
    List<String> privileges = document.field(P_PRIVILEGES, OType.EMBEDDEDLIST);
    List<String> roles = document.field(P_ROLES, OType.EMBEDDEDLIST);

    CRole entity = new CRole();
    entity.setId(id);
    entity.setName(name);
    entity.setDescription(description);
    entity.setPrivileges(privileges);
    entity.setRoles(roles);
    entity.setReadOnly(false);

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
   * Get all roles.
   */
  public Iterable<CRole> get(final ODatabaseDocumentTx db) {
    return Iterables.transform(browse(db), new Function<ODocument, CRole>()
    {
      @Nullable
      @Override
      public CRole apply(@Nullable final ODocument input) {
        return input == null ? null : read(input);
      }
    });
  }

  /**
   * Delete all roles.
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
