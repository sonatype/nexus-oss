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

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Adapter for converting between {@link Component} objects and OrientDB {@code ODocument}s.
 *
 * @since 3.0
 */
public interface ComponentEntityAdapter<T extends Component>
{
  String ORIENT_BASE_CLASS_NAME = new OClassNameBuilder().type(Component.class).build();

  /** OrientDB property name for the component's unique id, which is system-generated. */
  String P_ID = "id";

  /** OrientDB property name for the set of assets that comprise the component. */
  String P_ASSETS = "assets";

  /**
   * Gets the {@link Component} subclass this adapter works with.
   */
  Class<T> getComponentClass();

  /**
   * Creates the OrientDB class and associated indexes in the database if needed.
   */
  void registerStorageClass(ODatabaseDocumentTx db);

  /**
   * Converts a {@link Component} to an {@code ODocument}.
   */
  void convertToDocument(T component, ODocument document);

  /**
   * Converts an {@code ODocument} to a {@link Component}.
   */
  T convertToComponent(ODocument document);
}
