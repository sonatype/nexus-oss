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

import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Predicate;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;

/**
 * Supporting methods for {@link EntityAdapter} implementations.
 *
 * @since 3.0
 */
public abstract class EntityAdapterSupport
    extends ComponentSupport
{
  /**
   * Creates and initializes the given storage class, creating and initializing the base class first, if needed.
   */
  protected void createAndInitStorageClassWithBaseClass(OSchema schema,
                                                        String baseClassName,
                                                        Predicate<OClass> baseClassInitializer,
                                                        String storageClassName,
                                                        Predicate<OClass> storageClassInitializer) {
    OClass baseClass = schema.getClass(baseClassName);
    if (baseClass == null) {
      baseClass = schema.createAbstractClass(baseClassName);
      baseClassInitializer.apply(baseClass);
      logCreatedClassInfo(baseClass);
    }
    OClass storageClass = schema.getClass(storageClassName);
    if (storageClass == null) {
      storageClass = schema.createClass(storageClassName, baseClass);
      storageClassInitializer.apply(storageClass);
      logCreatedClassInfo(storageClass);
    }
  }

  /**
   * Convenience method for storing a {@code null} or a concrete value, converting it if needed.
   */
  protected void setValueOrNull(final ODocument document, final String propertyName, final Object value) {
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
  protected EntityId getEntityIdOrNull(ODocument document, String propertyName) {
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
  protected OProperty createOptionalProperty(OClass oClass, String propertyName, OType oType) {
    return oClass.createProperty(propertyName, oType);
  }

  /**
   * Creates a required property.
   */
  protected OProperty createRequiredProperty(OClass oClass, String propertyName, OType oType) {
    OProperty property = oClass.createProperty(propertyName, oType);
    property.setMandatory(true);
    property.setNotNull(true);
    return property;
  }

  /**
   * Creates a required property with an automatic index.
   */
  protected OProperty createRequiredAutoIndexedProperty(OClass oClass, String propertyName, OType oType, boolean unique) {
    OProperty property = createRequiredProperty(oClass, propertyName, oType);
    String indexName = String.format("%s.%s", oClass.getName(), propertyName);
    oClass.createIndex(indexName, unique ? INDEX_TYPE.UNIQUE : INDEX_TYPE.NOTUNIQUE, propertyName);
    return property;
  }

  /**
   * Logs an INFO message describing the class that was just created.
   */
  private void logCreatedClassInfo(OClass oClass) {
    log.info("Created class: {}, properties: {}, indexes: {}", oClass, oClass.properties(), oClass.getIndexes());
  }
}
