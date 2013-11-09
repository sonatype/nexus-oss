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

import javax.inject.Named;
import javax.servlet.Filter;

import org.sonatype.nexus.security.filter.authz.NexusTargetMappingAuthorizationFilter;
import org.sonatype.security.web.guice.SecurityWebFilter;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/**
 * Content module.
 *
 * @since 2.8
 */
@Named
public class ContentModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    bind(SecurityWebFilter.class);

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve("/content/*").with(ContentServlet.class);
        filter("/content/*").through(SecurityWebFilter.class);
      }
    });

    bindContentAuthcFilter("contentAuthcBasic", "Sonatype Nexus Repository Manager");

    bindTargetMappingFilter("contentTperms", "/content(.*)", "@1");
  }

  private void bindTargetMappingFilter(String name, String pathPrefix, String pathReplacement) {
    NexusTargetMappingAuthorizationFilter filter = new NexusTargetMappingAuthorizationFilter();
    filter.setPathPrefix(pathPrefix);
    filter.setPathReplacement(pathReplacement);
    bindNamedFilter(name, filter);
  }

  private void bindContentAuthcFilter(String name, String applicationName) {
    ContentAuthenticationFilter filter = new ContentAuthenticationFilter();
    filter.setApplicationName(applicationName);
    bindNamedFilter(name, filter);
  }

  private void bindNamedFilter(String name, Filter filter) {
    Key<Filter> key = Key.get(Filter.class, Names.named(name));
    bind(key).toProvider(defer(filter)).in(Scopes.SINGLETON);
  }

  /**
   * Guice injects bound instances eagerly, so to avoid missing dependencies causing eager failures when this module
   * is auto-installed (such as with Sisu's classpath scanning) we defer injection of the filter as much as possible.
   */
  @SuppressWarnings("unchecked")
  private Provider<Filter> defer(final Filter filter) {
    Class<Filter> impl = (Class<Filter>) filter.getClass();
    final MembersInjector<Filter> membersInjector = getMembersInjector(impl);
    bind(impl); // help with any auto-wiring dependency analysis

    return new Provider<Filter>()
    {
      public Filter get() {
        membersInjector.injectMembers(filter);
        return filter;
      }
    };
  }
}
