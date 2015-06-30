/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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

import org.sonatype.nexus.security.FilterChainModule;
import org.sonatype.nexus.security.SecurityFilter;
import org.sonatype.nexus.security.anonymous.AnonymousFilter;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

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
    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        serve("/*").with(WebResourceServlet.class);
        filter("/*").through(SecurityFilter.class);
      }
    });

    install(new FilterChainModule()
    {
      @Override
      protected void configure() {
        addFilterChain("/**", AnonymousFilter.NAME);
      }
    });
  }
}
