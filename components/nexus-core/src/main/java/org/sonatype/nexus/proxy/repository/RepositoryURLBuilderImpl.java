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
package org.sonatype.nexus.proxy.repository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class RepositoryURLBuilderImpl
    extends ComponentSupport
    implements RepositoryURLBuilder
{
  private final RepositoryRegistry repositoryRegistry;

  private final RepositoryTypeRegistry repositoryTypeRegistry;

  @Inject
  public RepositoryURLBuilderImpl(final RepositoryRegistry repositoryRegistry,
                                  final RepositoryTypeRegistry repositoryTypeRegistry)
  {
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    this.repositoryTypeRegistry = checkNotNull(repositoryTypeRegistry);
  }

  @Override
  public String getRepositoryContentUrl(final String repositoryId) throws NoSuchRepositoryException {
    return getRepositoryContentUrl(repositoryRegistry.getRepository(repositoryId));
  }

  @Override
  public String getRepositoryContentUrl(final Repository repository) {
    RepositoryTypeDescriptor rtd =
        repositoryTypeRegistry.getRepositoryTypeDescriptor(repository.getProviderRole(), repository.getProviderHint());

    String baseUrl;
    try {
      baseUrl = BaseUrlHolder.get();
    }
    catch (IllegalStateException e) {
      log.warn(e.toString());
      return null;
    }

    return String.format("%s/content/%s/%s", baseUrl, rtd.getPrefix(), repository.getPathPrefix());
  }

  @Override
  public String getExposedRepositoryContentUrl(final Repository repository) {
    if (!repository.isExposed()) {
      return null;
    }
    else {
      return getRepositoryContentUrl(repository);
    }
  }
}
