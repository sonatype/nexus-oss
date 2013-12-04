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

package org.sonatype.nexus.content.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.guice.FilterChainModule;
import org.sonatype.nexus.security.filter.FilterProviderSupport;
import org.sonatype.nexus.security.filter.authz.NexusTargetMappingAuthorizationFilter;
import org.sonatype.nexus.web.MdcUserContextFilter;
import org.sonatype.security.web.guice.SecurityWebFilter;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.RankingFunction;

import static org.sonatype.nexus.security.filter.FilterProviderSupport.filterKey;

/**
 * Content module.
 *
 * @since 2.8
 */
@Named
public class ContentModule
    extends AbstractModule
{
  private static final String CONTENT_MOUNT_POINT = "/content";

  @Override
  protected void configure() {
    // FIXME: Not sure why this is needed, but it appears to make things work (most of the time)
    bind(SecurityWebFilter.class);

    bind(filterKey("contentAuthcBasic")).to(ContentAuthenticationFilter.class).in(Singleton.class);
    bind(filterKey("contentTperms")).toProvider(ContentTargetMappingFilterProvider.class);

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve(CONTENT_MOUNT_POINT + "/*").with(ContentServlet.class);
        filter(CONTENT_MOUNT_POINT + "/*").through(SecurityWebFilter.class);
        filter(CONTENT_MOUNT_POINT + "/*").through(MdcUserContextFilter.class);
      }
    });

    install(new FilterChainModule()
    {
      @Override
      protected void configure() {
        addFilterChain(CONTENT_MOUNT_POINT + "/**", "noSessionCreation,contentAuthcBasic,contentTperms");
      }
    });

    // ROOT static resource + index
    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve("/*").with(StaticResourcesServlet.class);
      }
    });

    /*
     * Give components contributed by this plugin a low-level ranking (same level as Nexus core) so they are ordered
     * after components from other plugins. This makes sure all the non-root servlets will be invoked and this
     * one will not "grab all" of the requests as it's mounted on root. This helps plugins like Siesta and Restlet1x
     * to properly function, not be "layered" by the servlet mounted at root above.
     */
    bind(RankingFunction.class).toInstance(new DefaultRankingFunction(0));
  }

  @Singleton
  static class ContentTargetMappingFilterProvider
      extends FilterProviderSupport
  {
    @Inject
    public ContentTargetMappingFilterProvider(final NexusTargetMappingAuthorizationFilter filter) {
      super(filter);
      filter.setPathPrefix(CONTENT_MOUNT_POINT + "(.*)");
      filter.setPathReplacement("@1");
    }
  }
}
