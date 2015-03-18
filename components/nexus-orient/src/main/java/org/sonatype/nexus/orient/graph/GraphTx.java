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

package org.sonatype.nexus.orient.graph;

import java.util.List;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

/**
 * An {@code OrientGraph} that auto-closes, rolling back any changes that haven't been explicitly committed.
 *
 * It also handles registering any number of hooks, which will be auto-unregistered when closed.
 *
 * @since 3.0
 */
public class GraphTx
    extends OrientGraph
    implements AutoCloseable
{
  private final List<ORecordHook> hooks = Lists.newArrayList();

  public GraphTx(final ODatabaseDocumentTx db) {
    super(db);
    setUseLightweightEdges(true);
  }

  public void registerHook(ORecordHook hook) {
    hooks.add(hook);
    database.registerHook(hook);
  }

  @Override
  public void close() {
    for (ORecordHook hook : hooks) {
      database.unregisterHook(hook);
    }

    database.rollback(); // no-op if no changes have occurred since last commit
    shutdown();
  }
}
