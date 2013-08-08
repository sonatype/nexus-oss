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

package org.sonatype.nexus.plugins.siesta;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.guice.FilterChainModule;
import org.sonatype.nexus.web.MdcUserContextFilter;
import org.sonatype.security.web.guice.SecurityWebFilter;
import org.sonatype.sisu.siesta.common.Resource;
import org.sonatype.sisu.siesta.jackson.SiestaJacksonModule;
import org.sonatype.sisu.siesta.server.internal.ComponentDiscoveryApplication;
import org.sonatype.sisu.siesta.server.internal.ComponentDiscoveryReporter;
import org.sonatype.sisu.siesta.server.internal.ComponentDiscoveryReporterImpl;
import org.sonatype.sisu.siesta.server.internal.SiestaServlet;
import org.sonatype.sisu.siesta.server.internal.jersey.SiestaJerseyModule;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Siesta plugin module.
 *
 * @since 2.4
 */
@Named
public class SiestaModule
    extends AbstractModule
{

  private static final Logger log = LoggerFactory.getLogger(SiestaModule.class);

  public static final String SERVICE_NAME = "siesta";

  public static final String MOUNT_POINT = "/service/" + SERVICE_NAME;

  public static final String SKIP_MODULE_CONFIGURATION = SiestaModule.class.getName() + ".skip";

  @Override
  protected void configure() {
    // HACK: avoid configuration of this module in casses as it is not wanted. e.g. automatically discovered by sisu
    if (!Boolean.getBoolean(SKIP_MODULE_CONFIGURATION)) {
      doConfigure();
    }
  }

  private void doConfigure() {
    // FIXME: Sort this out... nexus-restlet1x-plugin should not have anything to do with this plugin

    // We need to import some components from nexus-restlet1x-plugin for SecurityWebFilter, but its use is
    // hidden behind guice-servlet muck. We therefore bind it explicitly here so it will get seen by Sisu.
    // It would have been preferable to use "requireBinding(SecurityWebFilter.class)" to import the
    // SecurityWebFilter instance from nexus-restlet1x-plugin, but guice-servlet only wants to see filters
    // bound directly as singletons in this Injector (odd limitation). An alternative would have been to
    // requireBinding's for SecuritySystem and FilterChainResolver, which are the filter's dependencies.

    bind(SecurityWebFilter.class);

    install(new org.sonatype.sisu.siesta.server.internal.SiestaModule());
    install(new SiestaJerseyModule());
    install(new SiestaJacksonModule());

    // Dynamically discover JAX-RS components
    bind(javax.ws.rs.core.Application.class).to(ComponentDiscoveryApplication.class).in(Singleton.class);

    // Customize the report to include the MOUNT_POINT
    bind(ComponentDiscoveryReporter.class).toInstance(new ComponentDiscoveryReporterImpl(log)
    {
      @Override
      protected String pathOf(final Class<Resource> type) {
        String path = super.pathOf(type);
        if (!path.startsWith("/")) {
          path = "/" + path;
        }
        return MOUNT_POINT + path;
      }
    });

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve(MOUNT_POINT + "/*").with(SiestaServlet.class);
        filter(MOUNT_POINT + "/*").through(SecurityWebFilter.class);
        filter(MOUNT_POINT + "/*").through(MdcUserContextFilter.class);
      }
    });

    install(new FilterChainModule()
    {
      @Override
      protected void configure() {
        addFilterChain(MOUNT_POINT + "/**", "noSessionCreation,authcBasic");
      }

    });
  }
}
