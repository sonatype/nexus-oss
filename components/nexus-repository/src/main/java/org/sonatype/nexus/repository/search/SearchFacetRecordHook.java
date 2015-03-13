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
package org.sonatype.nexus.repository.search;

import org.sonatype.nexus.orient.graph.GraphTx;
import org.sonatype.nexus.repository.storage.StorageFacet;

import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Search facet record hook.
 *
 * @since 3.0
 */
public class SearchFacetRecordHook
    extends ODocumentHookAbstract
{
  private final ComponentMetadataFactory componentMetadataFactory;

  private final GraphTx graphTx;

  private final SearchFacet searchFacet;

  public SearchFacetRecordHook(final ComponentMetadataFactory componentMetadataFactory,
                               final GraphTx graphTx,
                               final SearchFacet searchFacet)
  {
    this.componentMetadataFactory = checkNotNull(componentMetadataFactory);
    this.graphTx = checkNotNull(graphTx);
    this.searchFacet = checkNotNull(searchFacet);
    setIncludeClasses(StorageFacet.V_COMPONENT);
  }

  @Override
  public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
    return DISTRIBUTED_EXECUTION_MODE.TARGET_NODE;
  }

  @Override
  public void onRecordAfterCreate(final ODocument doc) {
    // TODO should indexing failures affect storage? (catch and log?)
    if (doc.getIdentity().isPersistent()) {
      searchFacet.put(componentMetadataFactory.from(new OrientVertex(graphTx, doc)));
    }
  }

  @Override
  public void onRecordAfterUpdate(final ODocument doc) {
    onRecordAfterCreate(doc);
  }

  @Override
  public void onRecordAfterDelete(final ODocument doc) {
    // TODO should indexing failures affect storage? (catch and log?)
    searchFacet.delete(new OrientVertex(graphTx, doc).getId().toString());
  }
}
