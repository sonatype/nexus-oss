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
package org.sonatype.nexus.extender;

import java.util.Map;

import javax.servlet.ServletContext;

import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.security.WebSecurityModule;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceFilter;
import org.eclipse.sisu.wire.ParameterKeys;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.name.Names.named;

/**
 * Binds the initial {@link ServletContext} and configures shiro.
 * 
 * @since 3.0
 */
public class NexusContextModule
    extends AbstractModule
{
  private final ServletContext servletContext;

  private final Map<?, ?> properties;

  public NexusContextModule(final ServletContext servletContext, final Map<?, ?> properties) {
    this.servletContext = checkNotNull(servletContext);
    this.properties = checkNotNull(properties);
  }

  @Override
  protected void configure() {
    binder().requireExplicitBindings();

    // key components we expect to be able to find after booting nexus
    requireBinding(Key.get(Lifecycle.class, Names.named("NxApplication")));
    requireBinding(GuiceFilter.class);
    requireBinding(LogManager.class);

    // re-bind context with name to avoid backwards-compat warnings from guice-servlet
    bind(ServletContext.class).annotatedWith(named("nexus")).toInstance(servletContext);
    bind(ParameterKeys.PROPERTIES).toInstance(properties);

    install(new WebSecurityModule(servletContext));
  }
}
