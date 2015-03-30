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
package org.sonatype.nexus.web;

import java.util.Map;

import javax.servlet.ServletContext;

import org.sonatype.nexus.internal.metrics.MetricsModule;
import org.sonatype.nexus.internal.orient.OrientModule;
import org.sonatype.nexus.internal.web.EnvironmentFilter;
import org.sonatype.nexus.internal.web.ErrorPageFilter;
import org.sonatype.nexus.internal.web.ErrorPageServlet;
import org.sonatype.nexus.security.WebSecurityModule;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.wire.ParameterKeys;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.name.Names.named;

/**
 * Core Nexus servlet bindings.
 * 
 * @since 3.0
 */
public class NexusServletModule
    extends AbstractModule
{
  private final ServletContext servletContext;

  private final Map<?, ?> properties;

  public NexusServletModule(final ServletContext servletContext, final Map<?, ?> properties) {
    this.servletContext = checkNotNull(servletContext);
    this.properties = checkNotNull(properties);
  }

  @Override
  protected void configure() {
    binder().requireExplicitBindings();

    bind(NexusGuiceFilter.class);

    // re-bind context with name to avoid backwards-compat warnings from guice-servlet
    bind(ServletContext.class).annotatedWith(named("nexus")).toInstance(servletContext);
    bind(ParameterKeys.PROPERTIES).toInstance(properties);

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        bind(EnvironmentFilter.class);
        bind(ErrorPageFilter.class);

        filter("/*").through(EnvironmentFilter.class);
        filter("/*").through(ErrorPageFilter.class);

        bind(ErrorPageServlet.class);

        serve("/error.html").with(ErrorPageServlet.class);

        // our configuration needs to be first-most when calculating order (some fudge room for edge-cases)
        bind(RankingFunction.class).toInstance(new DefaultRankingFunction(0x70000000));
      }
    });

    install(new MetricsModule());

    install(new WebSecurityModule(servletContext));

    install(new OrientModule());
  }
}
