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
package org.sonatype.nexus.index.tasks;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Publish indexes task.
 */
@Named
public class PublishIndexesTask
    extends RepositoryTaskSupport<Void>
{
  private final IndexerManager indexerManager;

  @Inject
  public PublishIndexesTask(final IndexerManager indexerManager)
  {
    this.indexerManager = checkNotNull(indexerManager);
  }

  @Override
  protected Void execute()
      throws Exception
  {
    try {
      if (getConfiguration().getRepositoryId() != null) {
        indexerManager.publishRepositoryIndex(getConfiguration().getRepositoryId());
      }
      else {
        indexerManager.publishAllIndex();
      }
    }
    catch (IOException e) {
      log.error("Cannot publish indexes!", e);
    }

    return null;
  }

  @Override
  public String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return "Publishing indexes for repository " + getConfiguration().getRepositoryId();
    }
    else {
      return "Publishing indexes for all registered repositories";
    }
  }

}
