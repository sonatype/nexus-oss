/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.EntityId;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static org.sonatype.nexus.component.services.adapter.EntityAdapter.P_ID;

/**
 * Supporting methods for {@link EntityAdapter} implementations.
 *
 * @since 3.0
 */
public abstract class EntityAdapterSupport
    extends ComponentSupport
{
  @Nullable
  protected EntityId entityId(ODocument document) {
    String uniqueString = document.field(P_ID);
    if (uniqueString == null) {
      return null;
    }
    else {
      return new EntityId(uniqueString);
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
   * Creates a required LINK property.
   */
  protected OProperty createRequiredLinkProperty(OClass oClass, String propertyName, OType linkType, OClass targetClass) {
    OProperty property = oClass.createProperty(propertyName, linkType, targetClass);
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
   * Creates a required LINK property with an automatic index.
   */
  protected OProperty createRequiredAutoIndexedLinkProperty(OClass oClass, String propertyName, OType linkType, OClass targetClass, boolean unique) {
    OProperty property = createRequiredLinkProperty(oClass, propertyName, linkType, targetClass);
    String indexName = String.format("%s.%s", oClass.getName(), propertyName);
    oClass.createIndex(indexName, unique ? INDEX_TYPE.UNIQUE : INDEX_TYPE.NOTUNIQUE, propertyName);
    return property;
  }

  /**
   * Logs an INFO message describing the class that was just created.
   */
  protected void logCreatedClassInfo(OClass oClass) {
    log.info("Created class: {}, properties: {}, indexes: {}", oClass, oClass.properties(), oClass.getIndexes());
  }
}
