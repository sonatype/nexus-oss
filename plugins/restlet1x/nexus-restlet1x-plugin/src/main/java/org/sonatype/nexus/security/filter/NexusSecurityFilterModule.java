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

package org.sonatype.nexus.security.filter;

import javax.inject.Named;
import javax.servlet.Filter;

import org.sonatype.nexus.security.filter.authc.NexusApiKeyAuthenticationFilter;
import org.sonatype.nexus.security.filter.authc.NexusContentAuthenticationFilter;
import org.sonatype.nexus.security.filter.authc.NexusSecureHttpAuthenticationFilter;
import org.sonatype.nexus.security.filter.authz.FailureLoggingHttpMethodPermissionFilter;
import org.sonatype.nexus.security.filter.authz.NexusTargetMappingAuthorizationFilter;
import org.sonatype.security.web.filter.authc.LogoutAuthenticationFilter;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;

/**
 * Sets up Nexus's security filter configuration; this is a @Named module so it will be auto-installed by Sisu.
 */
@Named
public class NexusSecurityFilterModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    requireBinding(FilterChainResolver.class);

    bindAuthcFilter("authcBasic", false, "Sonatype Nexus Repository Manager API");
    bindAuthcFilter("authcNxBasic", true, "Sonatype Nexus Repository Manager API (specialized auth)");

    bindNamedFilter("logout", new LogoutAuthenticationFilter());
    bindNamedFilter("perms", new FailureLoggingHttpMethodPermissionFilter());

    bindTargetMappingFilter("trperms", "/service/local/repositories/(.*)/content(.*)", "/repositories/@1@2");
    bindTargetMappingFilter("tiperms", "/service/local/repositories/(.*)/index_content(.*)", "/repositories/@1@2");

    bindTargetMappingFilter("tgperms", "/service/local/repo_groups/(.*)/content(.*)", "/groups/@1@2");
    bindTargetMappingFilter("tgiperms", "/service/local/repo_groups/(.*)/index_content(.*)", "/groups/@1@2");

    bindContentAuthcFilter("contentAuthcBasic", "Sonatype Nexus Repository Manager");

    bindTargetMappingFilter("contentTperms", "/content(.*)", "@1");

    bindApiKeyFilter("authcApiKey", "Sonatype Nexus Repository Manager API (X-...-ApiKey auth)");
  }

  private void bindAuthcFilter(String name, boolean fakeAuthSchem, String applicationName) {
    NexusSecureHttpAuthenticationFilter filter = new NexusSecureHttpAuthenticationFilter();
    filter.setApplicationName(applicationName);
    filter.setFakeAuthScheme(Boolean.toString(fakeAuthSchem));
    bindNamedFilter(name, filter);
  }

  private void bindTargetMappingFilter(String name, String pathPrefix, String pathReplacement) {
    NexusTargetMappingAuthorizationFilter filter = new NexusTargetMappingAuthorizationFilter();
    filter.setPathPrefix(pathPrefix);
    filter.setPathReplacement(pathReplacement);
    bindNamedFilter(name, filter);
  }

  /**
   * Binds special authc filter to be used for {code}/content{code} resources to handle authentication restrictions.
   */
  private void bindContentAuthcFilter(String name, String applicationName) {
    NexusContentAuthenticationFilter filter = new NexusContentAuthenticationFilter();
    filter.setApplicationName(applicationName);
    bindNamedFilter(name, filter);
  }

  private void bindApiKeyFilter(String name, String applicationName) {
    NexusSecureHttpAuthenticationFilter filter = new NexusApiKeyAuthenticationFilter();
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
