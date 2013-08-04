/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.tasks;

import org.sonatype.nexus.index.IndexerManager;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Reindex task.
 *
 * @author cstamas
 * @author Alin Dreghiciu
 */
@Component(role = ReindexTaskHandler.class, hint = "legacy")
public class ReindexTaskHandlerLegacy
    implements ReindexTaskHandler
{
  @Requirement
  private IndexerManager indexerManager;

  /**
   * Delegates to indexer manager.
   *
   * {@inheritDoc}
   */
  public void reindexAllRepositories(final String path,
                                     final boolean fullReindex)
      throws Exception
  {
    indexerManager.reindexAllRepositories(path, fullReindex);
  }

  /**
   * Delegates to indexer manager.
   *
   * {@inheritDoc}
   */
  public void reindexRepository(final String repositoryId,
                                final String path,
                                final boolean fullReindex)
      throws Exception
  {
    indexerManager.reindexRepository(path, repositoryId, fullReindex);
  }
}
