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
package org.sonatype.nexus.component.services.adapter;

import java.util.Date;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.model.Entity;
import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;
import org.slf4j.Logger;

/**
 * Adapter for converting between {@link Entity} objects and OrientDB {@code ODocument}s.
 *
 * @since 3.0
 */
public abstract class EntityAdapter<T extends Entity>
    extends ComponentSupport
{
  /** OrientDB property name for the entity's globally unique id. */
  public static final String P_ID = "id";

  /**
   * Gets the entity class this adapter works with.
   */
  public abstract Class<T> getEntityClass();

  /**
   * Creates the OrientDB class and associated indexes in the database if needed.
   */
  public abstract void registerStorageClass(ODatabaseDocumentTx db);

  /**
   * Sets the given document's properties based on those of the given entity.
   *
   * The following properties will not be set in the {@code ODocument} because the necessary information is not directly
   * available in the {@code Entity}. Setting these afterward, if needed, is the responsibility of the caller:
   * <ul>
   *   <li> For {@code component}-based documents: {@code P_ASSETS}</li>
   *   <li> For {@code asset}-based documents: {@code P_COMPONENT} and {@code P_BLOB_REFS}.</li>
   * </ul>
   */
  public abstract void convertToDocument(T entity, ODocument document);

  /**
   * Returns an entity with properties set based on those of the given document.
   *
   * The following properties will not be set in the {@code Entity} because the necessary information is not directly
   * available in the {@code ODocument}. Setting these afterward, if needed, is the responsibility of the caller:
   * <ul>
   *   <li> For {@link Component}-based entities: {@code assetIds}</li>
   *   <li> For {@link Asset}-based entities: {@code componentId}, {@code contentLength}, {@code lastModified},
   *        and {@code streamSupplier}.</li>
   * </ul>
   */
  public abstract T convertToEntity(ODocument document);

  /**
   * Convenience method for storing a {@code null} or a concrete value, converting it if needed.
   */
  protected static void setValueOrNull(final ODocument document, final String propertyName, final Object value) {
    if (value == null) {
      document.field(propertyName, (Object) null);
    }
    else if (value instanceof EntityId) {
      document.field(propertyName, ((EntityId) value).asUniqueString());
    }
    else if (value instanceof DateTime) {
      document.field(propertyName, ((DateTime) value).toDate());
    }
    else {
      document.field(propertyName, value);
    }
  }

  @Nullable
  protected static EntityId getEntityIdOrNull(ODocument document, String propertyName) {
    String uniqueString = document.field(propertyName);
    if (uniqueString == null) {
      return null;
    }
    else {
      return new EntityId(uniqueString);
    }
  }

  @Nullable
  protected static DateTime getDateTimeOrNull(ODocument document, String propertyName) {
    Date date = document.field(propertyName);
    if (date == null) {
      return null;
    }
    else {
      return new DateTime(date);
    }
  }

  /**
   * Creates an optional property.
   */
  protected static OProperty createOptionalProperty(OClass oClass, String propertyName, OType oType) {
    return oClass.createProperty(propertyName, oType);
  }

  /**
   * Creates a required property.
   */
  protected static OProperty createRequiredProperty(OClass oClass, String propertyName, OType oType) {
    OProperty property = oClass.createProperty(propertyName, oType);
    property.setMandatory(true);
    property.setNotNull(true);
    return property;
  }

  /**
   * Creates a required property with an automatic index.
   */
  protected static OProperty createRequiredAutoIndexedProperty(OClass oClass, String propertyName, OType oType, boolean unique) {
    OProperty property = createRequiredProperty(oClass, propertyName, oType);
    String indexName = String.format("%s.%s", oClass.getName(), propertyName);
    oClass.createIndex(indexName, unique ? INDEX_TYPE.UNIQUE : INDEX_TYPE.NOTUNIQUE, propertyName);
    return property;
  }

  /**
   * Logs an INFO message describing the class that was just created.
   */
  protected static void logCreatedClassInfo(Logger log, OClass oClass) {
    log.info("Created class: {}, properties: {}, indexes: {}", oClass, oClass.properties(), oClass.getIndexes());
  }
}
