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

package org.sonatype.nexus.configuration.application.runtime;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * The Class DefaultRuntimeConfigurationBuilder. Todo: all the bad thing is now concentrated in this class. We are
 * playing container instead of container.
 *
 * @author cstamas
 */
@Component(role = ApplicationRuntimeConfigurationBuilder.class)
public class DefaultApplicationRuntimeConfigurationBuilder
    extends AbstractLoggingComponent
    implements ApplicationRuntimeConfigurationBuilder
{
  @Requirement
  private PlexusContainer plexusContainer;

  @Override
  public Repository createRepositoryFromModel(final Configuration configuration, final CRepository repoConf)
      throws ConfigurationException
  {
    Repository repository = createRepository(repoConf.getProviderRole(), repoConf.getProviderHint());

    repository.configure(repoConf);

    return repository;
  }

  @Override
  public void releaseRepository(final Repository repository, final Configuration configuration,
                                final CRepository repoConf)
      throws ConfigurationException
  {
    try {
      plexusContainer.release(repository);
    }
    catch (ComponentLifecycleException e) {
      getLogger().warn(
          "Problem while unregistering repository {} from Nexus! This will not affect configuration but might occupy memory, that will be released after next reboot.",
          RepositoryStringUtils.getHumanizedNameString(repository), e);
    }
  }

  // ----------------------------------------
  // private stuff

  private Repository createRepository(final String role, final String hint)
      throws InvalidConfigurationException
  {
    try {
      return Repository.class.cast(plexusContainer.lookup(role, hint));
    }
    catch (ComponentLookupException e) {
      throw new InvalidConfigurationException("Could not lookup a new instance of Repository!", e);
    }
  }
}
