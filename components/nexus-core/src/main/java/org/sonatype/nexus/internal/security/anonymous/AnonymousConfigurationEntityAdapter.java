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
package org.sonatype.nexus.internal.security.anonymous;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.security.anonymous.AnonymousConfiguration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link AnonymousConfiguration} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class AnonymousConfigurationEntityAdapter
    extends ComponentSupport
{
  public static final String DB_CLASS = new OClassNameBuilder()
      .prefix("security")
      .type("anonymous")
      .build();

  public static final String P_ENABLED = "enabled";

  public static final String P_USER_ID = "userId";

  public static final String P_REALM_NAME = "realmName";

  /**
   * Register schema.
   */
  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);

      type.createProperty(P_ENABLED, OType.BOOLEAN).setMandatory(true).setNotNull(true);
      type.createProperty(P_USER_ID, OType.STRING).setNotNull(true);
      type.createProperty(P_REALM_NAME, OType.STRING).setNotNull(true);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  public ODocument create(final ODatabaseDocumentTx db, final AnonymousConfiguration entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Write entity to document.
   */
  public ODocument write(final ODocument document, final AnonymousConfiguration entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_ENABLED, entity.isEnabled());
    document.field(P_USER_ID, entity.getUserId());
    document.field(P_REALM_NAME, entity.getRealmName());

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public AnonymousConfiguration read(final ODocument document) {
    checkNotNull(document);

    boolean enabled = document.field(P_ENABLED, OType.BOOLEAN);
    String userId = document.field(P_USER_ID, OType.STRING);
    String realmName = document.field(P_REALM_NAME, OType.STRING);

    AnonymousConfiguration entity = new AnonymousConfiguration();
    entity.setEnabled(enabled);
    entity.setUserId(userId);
    entity.setRealmName(realmName);

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
