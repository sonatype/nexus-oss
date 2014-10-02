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
package org.sonatype.nexus.component.source.internal;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.config.ComponentSourceConfig;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link ComponentSourceConfig} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentSourceConfigEntityAdapter
    extends ComponentSupport
{
  public static final String DB_PREFIX = "viewConfig";

  public static final String DB_CLASS = new OClassNameBuilder()
      .prefix(DB_PREFIX)
      .type(ComponentSourceConfig.class)
      .build();

  public static final String P_SOURCENAME = "viewName";

  public static final String P_FACTORYNAME = "factoryName";

  public static final String P_CONFIGURATION = "configuration";

  /**
   * Register schema.
   */
  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);

      type.createProperty(P_SOURCENAME, OType.STRING).setNotNull(true);
      type.createIndex(P_SOURCENAME + "idx", INDEX_TYPE.UNIQUE, P_SOURCENAME);
      type.createProperty(P_FACTORYNAME, OType.STRING).setNotNull(true);
      type.createProperty(P_CONFIGURATION, OType.EMBEDDEDMAP);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  public ODocument create(final ODatabaseDocumentTx db, final ComponentSourceConfig entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Write entity to document.
   */
  public ODocument write(final ODocument document, final ComponentSourceConfig entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_SOURCENAME, entity.getSourceName());
    document.field(P_FACTORYNAME, entity.getFactoryName());
    document.field(P_CONFIGURATION, entity.getConfiguration());

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public ComponentSourceConfig read(final ODocument document) {
    checkNotNull(document);

    ComponentSourceConfig entity = new ComponentSourceConfig((String) document.field(P_SOURCENAME, OType.STRING),
        (String) document.field(P_FACTORYNAME, OType.STRING),
        (Map<String, Object>) document.field(P_CONFIGURATION, OType.EMBEDDEDMAP)
    );

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
