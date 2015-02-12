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
import org.sonatype.nexus.yum.internal.createrepo.YumPackage.File;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.yum.internal.createrepo.YumPackage.FileType;

/**
 * {@link File} entity adapter.
 *
 * @since 3.0
 */
public class YumPackageFileEntityAdapter
    extends ComponentSupport
{

  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("yum")
      .type(File.class)
      .build();

  private static final String P_NAME = "name";

  private static final String P_TYPE = "type";

  private static final String P_PRIMARY = "primary";

  /**
   * Register schema.
   */
  OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);
      type.createProperty(P_NAME, OType.STRING).setNotNull(true).setMandatory(true);
      type.createProperty(P_TYPE, OType.STRING).setNotNull(true).setMandatory(true);
      type.createProperty(P_PRIMARY, OType.BOOLEAN).setNotNull(true);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  ODocument create(final ODatabaseDocumentTx db, final File entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Create a new document and write entity.
   */
  List<ODocument> create(final ODatabaseDocumentTx db, final List<File> entities) {
    if (entities == null) {
      return null;
    }
    List<ODocument> documents = Lists.newArrayList();
    for (File entity : entities) {
      documents.add(create(db, entity));
    }
    return documents;
  }

  /**
   * Write entity to document.
   */
  ODocument write(final ODocument document, final File entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_NAME, entity.getName());
    document.field(P_TYPE, entity.getType().toString());
    document.field(P_PRIMARY, entity.getPrimary());

    return document;
  }

  /**
   * Read entity from document.
   */
  File read(final ODocument document) {
    checkNotNull(document);

    File entity = new File();
    entity.setName(document.<String>field(P_NAME, OType.STRING));
    entity.setType(FileType.valueOf(FileType.class, document.<String>field(P_TYPE, OType.STRING)));
    entity.setPrimary(document.<Boolean>field(P_PRIMARY, OType.BOOLEAN));

    return entity;
  }

  /**
   * Read entity from document.
   */
  List<File> read(final List<ODocument> documents) {
    if (documents == null) {
      return null;
    }
    List<File> entities = Lists.newArrayList();
    for (ODocument document : documents) {
      entities.add(read(document));
    }
    return entities;
  }

}
