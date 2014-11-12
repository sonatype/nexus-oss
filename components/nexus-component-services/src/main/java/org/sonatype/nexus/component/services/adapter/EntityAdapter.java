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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.model.EntityId;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Adapter for the abstract "entity" storage class.
 * <p>
 * This is the root storage class of all component and asset entities.
 *
 * @since 3.0
 */
@Named
@Singleton
public class EntityAdapter
    extends EntityAdapterSupport
{
  /** Storage class name. */
  public static final String CLASS_NAME = "entity";

  /**
   * Property name for an entity's globally unique id.
   * This is a system-controlled property whose value is an {@link EntityId}.
   */
  public static final String P_ID = "id";

  private final String className;

  private volatile boolean initialized;

  public EntityAdapter() {
    this(CLASS_NAME);
  }

  protected EntityAdapter(String className) {
    this.className = className;
  }

  /**
   * Gets the name of the storage class this adapter works with.
   */
  public String getClassName() {
    return className;
  }

  /**
   * Returns whether initialization has occurred on this adapter yet.
   *
   * @return {@code true} if {@link #getClass(OSchema)} has been called on this instance.
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Gets the OrientDB class, initializing it first if it doesn't exist yet.
   */
  public OClass getClass(OSchema schema) {
    OClass oClass = schema.getClass(CLASS_NAME);
    if (oClass == null) {
      oClass = schema.createAbstractClass(CLASS_NAME);
      createRequiredAutoIndexedProperty(oClass, P_ID, OType.STRING, true);
      logCreatedClassInfo(oClass);
    }
    initialized = true;
    return oClass;
  }

  /**
   * Creates the storage subclass and calls {@link #initClass(OClass)} on it if this adapter is a subclass of
   * {@code superClass} and the storage subclass doesn't yet exist in the schema.
   */
  protected void maybeCreateSubClass(OSchema schema, OClass oSuperClass, Class adapterSuperClass) {
    if (this.getClass() != adapterSuperClass) {
      OClass oSubClass = schema.getClass(getClassName());
      if (oSubClass == null) {
        oSubClass = schema.createClass(getClassName(), oSuperClass);
        initClass(oSubClass);
        logCreatedClassInfo(oSubClass);
      }
    }
  }

  /**
   * Initialize the given concrete storage subclass to hold entities of this type.
   */
  protected void initClass(OClass oClass) {
    // no-op; subclasses may override
  }
}
