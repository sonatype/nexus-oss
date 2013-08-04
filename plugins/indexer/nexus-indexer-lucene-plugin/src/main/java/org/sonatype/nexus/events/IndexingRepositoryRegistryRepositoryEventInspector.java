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
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Listens for events and manages IndexerManager by adding and removing indexing contexts.
 * <p>
 * This EventInspector component HAS TO BE sync!
 *
 * @author Toni Menzel
 * @author cstamas
 */
@Component(role = EventInspector.class, hint = "IndexingRepositoryRegistryRepositoryEventInspector")
public class IndexingRepositoryRegistryRepositoryEventInspector
    extends AbstractEventInspector
{
  @Requirement
  private IndexerManager indexerManager;

  @Requirement
  private RepositoryRegistry repoRegistry;

  protected IndexerManager getIndexerManager() {
    return indexerManager;
  }

  public boolean accepts(Event<?> evt) {
    return (evt instanceof RepositoryRegistryRepositoryEvent)
        || (evt instanceof RepositoryConfigurationUpdatedEvent);
  }

  public void inspect(Event<?> evt) {
    if (!accepts(evt)) {
      return;
    }

    Repository repository = null;
    if (evt instanceof RepositoryRegistryRepositoryEvent) {
      repository = ((RepositoryRegistryRepositoryEvent) evt).getRepository();
    }
    else if (evt instanceof RepositoryConfigurationUpdatedEvent) {
      repository = ((RepositoryConfigurationUpdatedEvent) evt).getRepository();
    }

    try {
      // check registry for existence, wont be able to do much
      // if doesn't exist yet
      repoRegistry.getRepositoryWithFacet(repository.getId(), MavenRepository.class);
      inspectForIndexerManager(evt, repository);
    }
    catch (NoSuchRepositoryException e) {
      getLogger().debug("Attempted to handle repository that isn't yet in registry");
    }
  }

  private void inspectForIndexerManager(Event<?> evt, Repository repository) {
    try {
      // we are handling repo events, like addition and removal
      if (evt instanceof RepositoryRegistryEventAdd) {
        getIndexerManager().addRepositoryIndexContext(repository.getId());
      }
      else if (evt instanceof RepositoryRegistryEventRemove) {
        getIndexerManager().removeRepositoryIndexContext(
            ((RepositoryRegistryEventRemove) evt).getRepository().getId(), true);
      }
      else if (evt instanceof RepositoryConfigurationUpdatedEvent) {
        getIndexerManager().updateRepositoryIndexContext(repository.getId());
      }
    }
    catch (Exception e) {
      getLogger().error("Could not maintain indexing contexts!", e);
    }
  }
}