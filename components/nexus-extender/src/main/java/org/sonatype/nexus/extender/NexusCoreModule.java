/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

import org.sonatype.nexus.extender.modules.InstrumentationModule;
import org.sonatype.nexus.extender.modules.ValidationModule;
import org.sonatype.nexus.internal.orient.OrientModule;
import org.sonatype.nexus.web.NexusGuiceFilter;
import org.sonatype.nexus.web.SecurityFilter;
import org.sonatype.nexus.web.internal.BaseUrlHolderFilter;
import org.sonatype.nexus.web.internal.CommonHeadersFilter;
import org.sonatype.nexus.web.internal.ErrorPageFilter;
import org.sonatype.nexus.web.internal.ErrorPageServlet;
import org.sonatype.nexus.web.metrics.MetricsModule;
import org.sonatype.security.web.guice.SecurityWebModule;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.RankingFunction;
import org.eclipse.sisu.wire.ParameterKeys;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.inject.name.Names.named;

/**
 * Nexus core guice module.
 * 
 * @since 3.0
 */
public class NexusCoreModule
    extends AbstractModule
{
  private final ServletContext servletContext;

  private final Map<?, ?> properties;

  public NexusCoreModule(final ServletContext servletContext, final Map<?, ?> properties) {
    this.servletContext = checkNotNull(servletContext);
    this.properties = checkNotNull(properties);
  }

  @Override
  protected void configure() {
    binder().requireExplicitBindings();

    bind(NexusGuiceFilter.class);

    // HACK: Re-bind servlet-context instance with a name to avoid backwards-compat warnings from guice-servlet
    bind(ServletContext.class).annotatedWith(named("nexus")).toInstance(servletContext);
    bind(ParameterKeys.PROPERTIES).toInstance(properties);

    bind(SecurityFilter.class);

    install(new ShiroAopModule());
    install(new InstrumentationModule());
    install(new ValidationModule());

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        filter("/*").through(BaseUrlHolderFilter.class);
        filter("/*").through(ErrorPageFilter.class);
        filter("/*").through(CommonHeadersFilter.class);

        serve("/error.html").with(ErrorPageServlet.class);

        // our configuration needs to be first-most when calculating order (some fudge room for edge-cases)
        bind(RankingFunction.class).toInstance(new DefaultRankingFunction(0x70000000));
      }
    });

    install(new MetricsModule());

    install(new SecurityWebModule(servletContext, true));

    install(new OrientModule());
  }
}
