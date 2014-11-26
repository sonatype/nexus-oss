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

import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static com.google.common.base.Preconditions.checkState;

/**
 * Adapter for the abstract "component" storage class, which extends from "entity".
 * <p>
 * This is the base storage class of all components. A component represents a reusable software
 * package and may logically contain any number of assets, representing files that comprise the
 * package. In addition, a component may have any number of format-specific properties.
 * <p>
 * Subclasses are generally expected to be {@code @Named} {@code @Singleton}s, define a
 * {@code CLASS_NAME} constant, and implement a no-arg constructor that calls {@code super(CLASS_NAME)}.
 * They should also override {@link EntityAdapter#initClass(OClass)} if they need to define additional
 * properties and/or indexes as part of the storage schema.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentAdapter
    extends EntityAdapter
{
  /** Storage class name. */
  public static final String CLASS_NAME = "component";

  /**
   * Property name for the set of assets that belong to a component.
   * This is a system-controlled property whose value is a {@code Set} of {@code EntityId}s.
   */
  public static final String P_ASSETS = "assets";

  /** Component properties whose values are system-controlled. */
  public static final Set<String> SYSTEM_PROPS = ImmutableSet.of(P_ID, P_ASSETS);

  /**
   * No-arg constructor for direct instances.
   */
  public ComponentAdapter() {
    this(CLASS_NAME);
    checkState(this.getClass() == ComponentAdapter.class, "Subclass must use super(className) constructor");
  }

  /**
   * Constructor for subclasses.
   */
  protected ComponentAdapter(String className) {
    super(className);
  }

  @Override
  public OClass getClass(OSchema schema) {
    OClass superClass = super.getClass(schema);
    OClass oClass = schema.getClass(CLASS_NAME);
    if (oClass == null) {
      oClass = schema.createAbstractClass(CLASS_NAME, superClass);
      createRequiredProperty(oClass, P_ASSETS, OType.LINKSET);
      logCreatedClassInfo(oClass);
    }
    maybeCreateSubClass(schema, oClass, ComponentAdapter.class);
    return oClass;
  }
}
