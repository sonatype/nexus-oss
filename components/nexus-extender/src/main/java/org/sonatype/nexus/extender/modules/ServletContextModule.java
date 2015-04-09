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
package org.sonatype.nexus.extender.modules;

import javax.inject.Named;
import javax.servlet.ServletContext;

import org.sonatype.nexus.extender.NexusContextModule;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.inject.Sources;

/**
 * Override guice-servlet's legacy {@link ServletContext} binding as it doesn't work well with multiple injectors.<br>
 * <br>
 * The replacement binding redirects to any @Named ServletContext key, which the {@link BeanLocator} will implicitly
 * match to the servlet context bound in {@link NexusContextModule}. This redirect is deliberately hidden from the
 * BeanLocator to avoid any chance of an endless lookup cycle.
 * 
 * @since 3.0
 */
public class ServletContextModule
    extends AbstractModule
{
  private static final Key<ServletContext> NAMED_SERVLET_CONTEXT = Key.get(ServletContext.class, Named.class);

  @Override
  protected void configure() {
    requireBinding(NAMED_SERVLET_CONTEXT); // tell the BeanLocator it should supply this

    // override the original binding with hidden redirect to the one supplied by the locator
    binder().withSource(Sources.hide()).bind(ServletContext.class).to(NAMED_SERVLET_CONTEXT);
  }
}
