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
package org.sonatype.nexus.repository.storage;

import org.sonatype.nexus.common.time.Clock;
import org.sonatype.nexus.orient.graph.GraphTx;

import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Search facet record hook.
 *
 * @since 3.0
 */
public class LastUpdatedRecordHook
    extends ODocumentHookAbstract
{
  private final GraphTx graphTx;

  private final Clock clock;

  public LastUpdatedRecordHook(final GraphTx graphTx)
  {
    this.graphTx = checkNotNull(graphTx);
    this.clock = new Clock();
    setIncludeClasses(StorageFacet.V_COMPONENT);
    setIncludeClasses(StorageFacet.V_ASSET);
  }

  @Override
  public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
    return DISTRIBUTED_EXECUTION_MODE.TARGET_NODE;
  }

  @Override
  public RESULT onRecordBeforeCreate(final ODocument doc) {
    if (doc.getIdentity().isPersistent()) {
      new OrientVertex(graphTx, doc).setProperty(StorageFacet.P_LAST_UPDATED, clock.dateTime().toDate());
      return RESULT.RECORD_CHANGED;
    }
    else {
      return RESULT.RECORD_NOT_CHANGED;
    }
  }

  @Override
  public RESULT onRecordBeforeUpdate(final ODocument doc) {
    return onRecordBeforeCreate(doc);
  }
}
