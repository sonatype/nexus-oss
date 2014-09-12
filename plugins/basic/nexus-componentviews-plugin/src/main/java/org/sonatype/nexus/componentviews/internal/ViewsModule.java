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
package org.sonatype.nexus.componentviews.internal;

import javax.inject.Named;

import org.sonatype.nexus.componentviews.config.ViewFactorySource;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.componentviews.internal.orient.OrientViewConfigStore;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice servlet module for exposing the {@link ComponentViewServlet} to the servlet container.
 *
 * @since 3.0
 */
@Named
public class ViewsModule
    extends AbstractModule
{
  private static final Logger log = LoggerFactory.getLogger(ViewsModule.class);

  public static final String MOUNT_POINT = "/views/*";

  @Override
  protected void configure() {
    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        bind(ComponentViewServlet.class);
        serve("/views").with(ComponentViewServlet.class);
        serve("/views/*").with(ComponentViewServlet.class);
      }
    });
    log.info("Nexus component views configured.");
  }
}
