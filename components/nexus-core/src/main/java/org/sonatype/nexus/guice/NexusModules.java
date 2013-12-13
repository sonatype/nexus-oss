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

package org.sonatype.nexus.guice;

import javax.servlet.ServletContext;

import org.sonatype.nexus.web.BaseUrlHolderFilter;
import org.sonatype.nexus.web.ErrorPageFilter;
import org.sonatype.security.web.guice.SecurityWebModule;

import com.google.inject.AbstractModule;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.yammer.metrics.guice.InstrumentationModule;
import org.apache.shiro.guice.aop.ShiroAopModule;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.RankingFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Nexus guice modules.
 *
 * @since 2.4
 */
public class NexusModules
{
  /**
   * Nexus common guice module.
   */
  public static class CommonModule
      extends AbstractModule
  {
    @Override
    protected void configure() {
      install(new ShiroAopModule());
      install(new InstrumentationModule());
    }
  }

  /**
   * Nexus core guice module.
   */
  public static class CoreModule
      extends AbstractModule
  {
    private final ServletContext servletContext;

    public CoreModule(final ServletContext servletContext) {
      this.servletContext = checkNotNull(servletContext);
    }

    @Override
    protected void configure() {
      install(new CommonModule());

      install(new ServletModule()
      {
        @Override
        protected void configureServlets() {
          filter("/*").through(BaseUrlHolderFilter.class);
          filter("/*").through(ErrorPageFilter.class);

          // our configuration needs to be first-most when calculating order (some fudge room for edge-cases)
          bind(RankingFunction.class).toInstance(new DefaultRankingFunction(0x70000000));
        }
      });

      install(new SecurityWebModule(servletContext, true)
      {
        @Override
        protected void configureShiroWeb() {
          super.configureShiroWeb();

          // Expose an explicit binding to replace the old stateless and stateful "nexus" RealmSecurityManager with the
          // default RealmSecurityManager, since we now use the "noSessionCreation" filter in Shiro 1.2 on all services
          // except the login service.
          // The NexusWebRealmSecurityManager is still available (if necessary) under the "stateless-and-stateful" hint.
          Named nexus = Names.named("nexus");
          bind(RealmSecurityManager.class).annotatedWith(nexus).to(RealmSecurityManager.class);
          expose(RealmSecurityManager.class).annotatedWith(nexus);
        }
      });
    }
  }

  /**
   * Nexus plugin guice module.
   */
  public static class PluginModule
      extends AbstractModule
  {
    @Override
    protected void configure() {
      install(new CommonModule());
    }
  }
}
