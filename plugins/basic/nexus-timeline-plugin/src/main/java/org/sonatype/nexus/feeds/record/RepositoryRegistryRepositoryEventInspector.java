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

package org.sonatype.nexus.feeds.record;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.plexus.appevents.Event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Creates timeline entries (for feeds) about repository configuration related changes.
 *
 * @author Juven Xu
 */
@Component(role = EventInspector.class, hint = "RepositoryRegistryRepositoryEvent")
public class RepositoryRegistryRepositoryEventInspector
    extends AbstractFeedRecorderEventInspector
    implements AsynchronousEventInspector
{
  @Requirement
  private RepositoryRegistry repoRegistry;

  public boolean accepts(Event<?> evt) {
    return ((evt instanceof RepositoryRegistryRepositoryEvent) || (evt instanceof RepositoryConfigurationUpdatedEvent))
        && isNexusStarted();
  }

  public void inspect(Event<?> evt) {
    Repository repository = null;

    if (evt instanceof RepositoryRegistryRepositoryEvent) {
      repository = ((RepositoryRegistryRepositoryEvent) evt).getRepository();
    }
    else {
      repository = ((RepositoryConfigurationUpdatedEvent) evt).getRepository();
    }

    try {
      // check registry for existence, wont be able to do much
      // if doesn't exist yet
      repoRegistry.getRepository(repository.getId());

      inspectForTimeline(evt, repository);
    }
    catch (NoSuchRepositoryException e) {
      getLogger().debug("Attempted to handle repository that isn't yet in registry");
    }
  }

  private void inspectForTimeline(Event<?> evt, Repository repository) {
    // we do not want RSS entries about boot and repo additions during boot
    StringBuilder sb = new StringBuilder();

    if (repository.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
      sb.append(" repository group ");
    }
    else {
      sb.append(" repository ");
    }

    sb.append(repository.getName());

    sb.append(" (ID=");

    sb.append(repository.getId());

    sb.append(") ");

    if (repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
      sb.append(" as proxy repository for URL ");

      sb.append(repository.adaptToFacet(ProxyRepository.class).getRemoteUrl());
    }
    else if (repository.getRepositoryKind().isFacetAvailable(HostedRepository.class)) {
      sb.append(" as hosted repository");
    }
    else if (repository.getRepositoryKind().isFacetAvailable(ShadowRepository.class)) {
      sb.append(" as ");

      sb.append(repository.getClass().getName());

      sb.append(" virtual repository for ");

      sb.append(repository.adaptToFacet(ShadowRepository.class).getMasterRepository().getName());

      sb.append(" (ID=");

      sb.append(repository.adaptToFacet(ShadowRepository.class).getMasterRepository().getId());

      sb.append(") ");
    }

    sb.append(".");

    if (evt instanceof RepositoryRegistryEventAdd) {
      sb.insert(0, "Registered");
    }
    else if (evt instanceof RepositoryRegistryEventRemove) {
      sb.insert(0, "Unregistered");
    }
    else if (evt instanceof RepositoryConfigurationUpdatedEvent) {
      sb.insert(0, "Updated");
    }

    getFeedRecorder().addSystemEvent(FeedRecorder.SYSTEM_CONFIG_ACTION, sb.toString());
  }
}
