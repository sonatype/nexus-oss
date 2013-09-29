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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.inject.Key;
import com.google.inject.name.Names;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link ApplicationRuntimeConfigurationBuilder}.
 */
@Singleton
@Named
public class DefaultApplicationRuntimeConfigurationBuilder
    extends AbstractLoggingComponent
    implements ApplicationRuntimeConfigurationBuilder
{
  private final BeanLocator beanLocator;

  @Inject
  public DefaultApplicationRuntimeConfigurationBuilder(final BeanLocator beanLocator) {
    this.beanLocator = checkNotNull(beanLocator);
  }

  @Override
  public Repository createRepository(Class<? extends Repository> type, String name) throws ConfigurationException {
    try {
      final Provider<? extends Repository> rp = beanLocator.locate(Key.get(type, Names.named(name))).iterator().next()
          .getProvider();
      return rp.get();
    }
    catch (Exception e) {
      throw new InvalidConfigurationException("Could not lookup a new instance of Repository!", e);
    }
  }

  @Override
  public void releaseRepository(final Repository repository) throws ConfigurationException {
    if (repository == null) {
      return;
    }
    repository.dispose();
  }
}
