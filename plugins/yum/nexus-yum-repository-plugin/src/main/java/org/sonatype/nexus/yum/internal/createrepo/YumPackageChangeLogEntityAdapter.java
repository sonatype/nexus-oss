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
package org.sonatype.nexus.yum.internal.createrepo;

import java.util.List;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.yum.internal.createrepo.YumPackage.ChangeLog;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link ChangeLog} entity adapter.
 *
 * @since 3.0
 */
public class YumPackageChangeLogEntityAdapter
    extends ComponentSupport
{

  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("yum")
      .type(ChangeLog.class)
      .build();

  private static final String P_AUTHOR = "author";

  private static final String P_DATE = "date";

  private static final String P_TEXT = "text";

  /**
   * Register schema.
   */
  OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);
      type.createProperty(P_AUTHOR, OType.STRING);
      type.createProperty(P_DATE, OType.INTEGER);
      type.createProperty(P_TEXT, OType.STRING);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  ODocument create(final ODatabaseDocumentTx db, final ChangeLog entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Create a new document and write entity.
   */
  List<ODocument> create(final ODatabaseDocumentTx db, final List<ChangeLog> entities) {
    if (entities == null) {
      return null;
    }
    List<ODocument> documents = Lists.newArrayList();
    for (ChangeLog entity : entities) {
      documents.add(create(db, entity));
    }
    return documents;
  }

  /**
   * Write entity to document.
   */
  ODocument write(final ODocument document, final ChangeLog entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_AUTHOR, entity.getAuthor());
    document.field(P_DATE, entity.getDate());
    document.field(P_TEXT, entity.getText());

    return document;
  }

  /**
   * Read entity from document.
   */
  ChangeLog read(final ODocument document) {
    checkNotNull(document);

    ChangeLog entity = new ChangeLog();
    entity.setAuthor(document.<String>field(P_AUTHOR, OType.STRING));
    entity.setDate(document.<Integer>field(P_DATE, OType.INTEGER));
    entity.setText(document.<String>field(P_TEXT, OType.STRING));

    return entity;
  }

  /**
   * Read entity from document.
   */
  List<ChangeLog> read(final List<ODocument> documents) {
    if (documents == null) {
      return null;
    }
    List<ChangeLog> entities = Lists.newArrayList();
    for (ODocument document : documents) {
      entities.add(read(document));
    }
    return entities;
  }

}
