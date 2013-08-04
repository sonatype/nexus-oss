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

package org.sonatype.nexus.events;

import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Event inspector that maintains indexes.
 *
 * @author cstamas
 */
@Component(role = EventInspector.class, hint = "LuceneIndexerManagerEventInspector")
public class IndexerManagerEventInspector
    extends AbstractEventInspector
    implements AsynchronousEventInspector
{
  private final boolean enabled =
      SystemPropertiesHelper.getBoolean("org.sonatype.nexus.events.IndexerManagerEventInspector.enabled", true);

  private final boolean async =
      SystemPropertiesHelper.getBoolean("org.sonatype.nexus.events.IndexerManagerEventInspector.async", true);

  @Requirement
  private IndexerManager indexerManager;

  protected IndexerManager getIndexerManager() {
    return indexerManager;
  }

  @Override
  public boolean accepts(final Event<?> evt) {
    // listen for STORE, CACHE, DELETE only
    final boolean accepts = enabled
        && (evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventCache ||
        evt instanceof RepositoryItemEventDelete);
    if (!async && accepts) {
      inspectForIndexerManager(evt);
    }
    return accepts;
  }

  @Override
  public void inspect(final Event<?> evt) {
    if (async) {
      inspectForIndexerManager(evt);
    }
  }

  private void inspectForIndexerManager(final Event<?> evt) {
    RepositoryItemEvent ievt = (RepositoryItemEvent) evt;

    Repository repository = ievt.getRepository();

    // should we sync at all
    if (repository != null && repository.isIndexable()) {
      try {
        if (ievt instanceof RepositoryItemEventCache || ievt instanceof RepositoryItemEventStore) {
          getIndexerManager().addItemToIndex(repository, ievt.getItem());
        }
        else if (ievt instanceof RepositoryItemEventDelete) {
          getIndexerManager().removeItemFromIndex(repository, ievt.getItem());
        }
      }
      catch (Exception e) // TODO be more specific
      {
        getLogger().error("Could not maintain index for repository {}!", repository.getId(), e);
      }
    }
  }

}
