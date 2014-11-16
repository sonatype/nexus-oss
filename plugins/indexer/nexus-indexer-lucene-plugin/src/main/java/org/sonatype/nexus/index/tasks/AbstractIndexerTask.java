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
package org.sonatype.nexus.index.tasks;

import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.scheduling.Cancelable;
import org.sonatype.nexus.scheduling.RepositoryTaskSupport;

/**
 * Base class for indexer related tasks.
 */
public abstract class AbstractIndexerTask
    extends RepositoryTaskSupport<Void>
    implements Cancelable
{
  private List<ReindexTaskHandler> handlers;

  private String action;

  private boolean fullReindex;

  public AbstractIndexerTask(String action, boolean fullReindex) {
    this.action = action;
    this.fullReindex = fullReindex;
  }

  @Inject
  public void setReindexTaskHandlers(final List<ReindexTaskHandler> handlers) {
    this.handlers = handlers;
  }

  @Override
  public Void execute()
      throws Exception
  {
    for (ReindexTaskHandler handler : handlers) {
      try {
        if (getConfiguration().getRepositoryId() != null) {
          handler.reindexRepository(getConfiguration().getRepositoryId(), getConfiguration().getPath(), fullReindex);
        }
        else {
          handler.reindexAllRepositories(getConfiguration().getPath(), fullReindex);
        }
      }
      catch (NoSuchRepositoryException nsre) {
        // TODO: When we get to implement NEXUS-3977/NEXUS-1002 we'll be able to stop the indexing task when the
        // repo is deleted, so this exception handling/warning won't be needed anymore.
        if (getConfiguration().getRepositoryId() != null) {
          log.warn(
              "Repository with ID={} was not found. It's likely that the repository was deleted while either the repair or the update index task was running.",
              getConfiguration().getRepositoryId());
        }

        throw nsre;
      }
    }

    return null;
  }

  @Override
  protected String getMessage() {
    if (getConfiguration().getRepositoryId() != null) {
      return action + " repository index " + getConfiguration().getRepositoryId() + " from path " +
          getConfiguration().getPath() + " and below.";
    }
    else {
      return action + " all registered repositories index";
    }
  }

}