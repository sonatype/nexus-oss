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

package org.sonatype.nexus.plugins.mac;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryEventLocalStatusChanged;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.plexus.appevents.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * EventInspector that listens to registry events, repo addition and removal, and simply "hooks" in the generated
 * Archetype catalog file to their root.
 *
 * @author cstamas
 */
@Named
@Singleton
public class MacPluginEventInspector
    extends AbstractLoggingComponent
    implements EventInspector
{
  private static final String ARCHETYPE_PATH = "/archetype-catalog.xml";

  private final ContentClass maven2ContentClass;

  @Inject
  public MacPluginEventInspector(final @Named("maven2") ContentClass maven2ContentClass) {
    this.maven2ContentClass = checkNotNull(maven2ContentClass);
  }

  private boolean HANDLED(final Repository repository) {
    return maven2ContentClass.isCompatible(repository.getRepositoryContentClass())
        && (repository.getRepositoryKind().isFacetAvailable(HostedRepository.class)
        || repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class) ||
        repository.getRepositoryKind().isFacetAvailable(
            GroupRepository.class));
  }


  public boolean accepts(Event<?> evt) {
    if (evt instanceof RepositoryRegistryEventAdd) {
      // only if is relevant to us
      return HANDLED(((RepositoryRegistryEventAdd) evt).getRepository());
    }
    else if (evt instanceof RepositoryConfigurationUpdatedEvent) {
      // only if is relevant to us
      return HANDLED(((RepositoryConfigurationUpdatedEvent) evt).getRepository());
    }
    else if (evt instanceof RepositoryEventLocalStatusChanged) {
      // only if put into service
      return HANDLED(((RepositoryEventLocalStatusChanged) evt).getRepository())
          && LocalStatus.IN_SERVICE.equals(((RepositoryEventLocalStatusChanged) evt).getNewLocalStatus());
    }
    else {
      return false;
    }
  }

  public void inspect(Event<?> evt) {
    Repository repository = null;

    if (evt instanceof RepositoryRegistryEventAdd) {
      repository = ((RepositoryRegistryEventAdd) evt).getRepository();
    }
    else if (evt instanceof RepositoryConfigurationUpdatedEvent) {
      repository = ((RepositoryConfigurationUpdatedEvent) evt).getRepository();
    }
    else if (evt instanceof RepositoryEventLocalStatusChanged) {
      repository = ((RepositoryEventLocalStatusChanged) evt).getRepository();
    }
    else {
      // huh?
      return;
    }

    if (repository.isIndexable()) {
      // "install" the archetype catalog
      try {
        final DefaultStorageFileItem file =
            new DefaultStorageFileItem(repository, new ResourceStoreRequest(ARCHETYPE_PATH), true, false,
                new StringContentLocator(ArchetypeContentGenerator.ID));
        file.setContentGeneratorId(ArchetypeContentGenerator.ID);
        repository.storeItem(false, file);
      }
      catch (RepositoryNotAvailableException e) {
        getLogger().info("Unable to install the generated archetype catalog, repository {} is out of service",
            e.getRepository().getId());
      }
      catch (Exception e) {
        if (getLogger().isDebugEnabled()) {
          getLogger().info("Unable to install the generated archetype catalog in repository {}:", repository, e);
        }
        else {
          getLogger().info("Unable to install the generated archetype catalog in repository {}: {}/{}", repository,
              e.getClass(), e.getMessage());
        }
      }
    }
    else {
      // "uninstall" the archetype catalog
      try {
        repository.deleteItem(false, new ResourceStoreRequest(ARCHETYPE_PATH));
      }
      catch (RepositoryNotAvailableException e) {
        getLogger().info("Unable to uninstall the generated archetype catalog, repository {} is out of service",
            e.getRepository().getId());
      }
      catch (ItemNotFoundException e) {
        // neglect this, it was not present
      }
      catch (Exception e) {
        if (getLogger().isDebugEnabled()) {
          getLogger().info("Unable to uninstall the generated archetype catalog in repository {}:", repository, e);
        }
        else {
          getLogger().info("Unable to uninstall the generated archetype catalog in repository {}: {}/{}", repository,
              e.getClass(), e.getMessage());
        }
      }
    }
  }
}
