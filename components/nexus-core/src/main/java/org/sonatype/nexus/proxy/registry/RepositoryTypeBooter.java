/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.registry;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;

import com.google.inject.Key;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.inject.BeanLocator;

/**
 * Manages {@link RepositoryTypeDescriptor} registrations via Sisu component mediation.
 * 
 * @since 3.0
 */
@Named
@EagerSingleton
public class RepositoryTypeBooter
{
  @Inject
  public RepositoryTypeBooter(final BeanLocator locator, final RepositoryTypeRegistry registry) {
    final RepositoryTypeMediator repositoryTypeMediator = new RepositoryTypeMediator();

    locator.watch((Key) Key.get(GroupRepository.class), repositoryTypeMediator, registry);
    locator.watch((Key) Key.get(ShadowRepository.class), repositoryTypeMediator, registry);
    locator.watch((Key) Key.get(WebSiteRepository.class), repositoryTypeMediator, registry);
    locator.watch(Key.get(Repository.class), repositoryTypeMediator, registry);
  }

  public static class RepositoryTypeMediator
      implements Mediator<Named, Repository, RepositoryTypeRegistry>
  {
    public void add(final BeanEntry<Named, Repository> entry, final RepositoryTypeRegistry registry) {
      registry.registerRepositoryTypeDescriptors(buildDescriptor(entry));
    }

    public void remove(final BeanEntry<Named, Repository> entry, final RepositoryTypeRegistry registry) {
      registry.unregisterRepositoryTypeDescriptors(buildDescriptor(entry));
    }

    private static RepositoryTypeDescriptor buildDescriptor(final BeanEntry<Named, Repository> entry) {
      final Class<?> implementationClass = entry.getImplementationClass();
      final String hint = entry.getKey().value();

      if (GroupRepository.class.isAssignableFrom(implementationClass)) {
        return new RepositoryTypeDescriptor(GroupRepository.class, hint, "groups");
      }
      if (ShadowRepository.class.isAssignableFrom(implementationClass)) {
        return new RepositoryTypeDescriptor(ShadowRepository.class, hint, "shadows");
      }
      if (WebSiteRepository.class.isAssignableFrom(implementationClass)) {
        return new RepositoryTypeDescriptor(WebSiteRepository.class, hint, "sites");
      }
      return new RepositoryTypeDescriptor(Repository.class, hint, "repositories");
    }
  }
}
