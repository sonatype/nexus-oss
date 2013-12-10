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

package org.sonatype.nexus.webresources.internal;

import javax.inject.Named;

import org.sonatype.nexus.web.ErrorPageFilter;
import org.sonatype.nexus.web.TemplateRenderer;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.eclipse.sisu.inject.DefaultRankingFunction;
import org.eclipse.sisu.inject.RankingFunction;

/**
 * Web resources module.
 *
 * @since 2.8
 */
@Named
public class WebResourcesModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    requireBinding(TemplateRenderer.class);

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve("/*").with(WebResourcesServlet.class);
        filter("/*").through(ErrorPageFilter.class);

        // Give components contributed by this plugin a low-level ranking (same level as Nexus core) so they are ordered
        // after components from other plugins. This makes sure all the their non-root servlets will be invoked and this
        // one will not "grab all" of the requests as it's mounted on root.
        bind(RankingFunction.class).toInstance(new DefaultRankingFunction(0));
      }
    });
  }
}
