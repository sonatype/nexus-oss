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
import org.sonatype.nexus.component.recordstore.Record;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Converts component domain objects to orient {@code ODocument} objects, and vice-versa.
 *
 * @since 3.0
 */
public interface ComponentEntityAdapter<T extends Component>
{
  /**
   * Gets the component class.
   */
  Class<T> getComponentClass();

  /**
   * Gets the OrientDB class, creating it in the database if it doesn't already exist.
   */
  OClass getDocumentClass(ODatabaseDocumentTx db);

  /**
   * Converts a component to a document object.
   */
  void componentToDocument(T component, ODocument document);

  /**
   * Converts a document to a component object.
   */
  T documentToComponent(Record record);
}
