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
package org.sonatype.nexus.webapp.metrics;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.inject.Key;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.Mediator;
import org.eclipse.sisu.inject.BeanLocator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper to manage {@link HealthCheck} registrations via Sisu component mediation.
 *
 * @since 2.8
 */
@Named
@EagerSingleton
public class HealthCheckRegistrar
    extends ComponentSupport
{
  @Inject
  public HealthCheckRegistrar(final BeanLocator beanLocator,
                              final HealthCheckRegistry registry)
  {
    checkNotNull(beanLocator);
    checkNotNull(registry);
    beanLocator.watch(Key.get(HealthCheck.class), new HealthCheckMediator(), registry);
  }

  private class HealthCheckMediator
      implements Mediator<Annotation, HealthCheck, HealthCheckRegistry>
  {
    public void add(final BeanEntry<Annotation, HealthCheck> entry, final HealthCheckRegistry registry)
        throws Exception
    {
      log.debug("Registering: {}", entry);
      registry.register(entry.getValue());
    }

    public void remove(final BeanEntry<Annotation, HealthCheck> entry, final HealthCheckRegistry registry)
        throws Exception
    {
      log.debug("Un-registering: {}", entry);
      registry.unregister(entry.getValue());
    }
  }
}
