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

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class for adapters that convert between concrete {@link Component}s and OrientDB {@code ODocument}s.
 *
 * @since 3.0
 */
public abstract class ComponentEntityAdapter
{
  public static String ORIENT_CLASS_NAME = new OClassNameBuilder().type(Component.class).build();

  /** OrientDB property name for the component's unique id, which is system-generated. */
  public static String P_ID = "id";

  /** OrientDB property name for the set of assets that comprise the component. */
  public static String P_ASSETS = "assets";

  /**
   * Creates the base OrientDB class and associated indexes in the database if needed.
   */
  public static void registerBaseClass(OSchema schema, Logger log) {
    checkNotNull(schema);
    if (!schema.existsClass(ORIENT_CLASS_NAME)) {
      OClass oClass = schema.createAbstractClass(ORIENT_CLASS_NAME);
      EntityAdapter.createRequiredAutoIndexedProperty(oClass, P_ID, OType.STRING, true);
      EntityAdapter.createRequiredProperty(oClass, P_ASSETS, OType.LINKSET);
      EntityAdapter.logCreatedClassInfo(log, oClass);
    }
  }

  /**
   * Adds base properties from the given {@code Component} to the given {@code ODocument}
   */
  public static void convertBasePropertiesToDocument(Component entity, ODocument document) {
    EntityAdapter.setValueOrNull(document, P_ID, entity.getId());
  }

  /**
   * Adds base properties from the given {@code ODocument} to the given {@code Component}.
   */
  public static void convertBasePropertiesToEntity(ODocument document, Component entity) {
    entity.setId(EntityAdapter.getEntityIdOrNull(document, P_ID));
  }
}
