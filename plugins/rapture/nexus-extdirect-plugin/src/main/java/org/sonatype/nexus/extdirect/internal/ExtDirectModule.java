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

package org.sonatype.nexus.extdirect.internal;

import java.util.Map;

import javax.inject.Named;

import org.sonatype.nexus.web.internal.SecurityFilter;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import com.softwarementors.extjs.djn.servlet.DirectJNgineServlet.GlobalParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ext.Direct module.
 *
 * @since 2.8
 */
@Named
public class ExtDirectModule
    extends AbstractModule
{

  private static final Logger log = LoggerFactory.getLogger(ExtDirectModule.class);

  private static final String MOUNT_POINT = "/service/extdirect";

  @Override
  protected void configure() {
    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        Map<String, String> directServletConfig = Maps.newHashMap();
        directServletConfig.put(GlobalParameters.PROVIDERS_URL, MOUNT_POINT.substring(1));
        directServletConfig.put(GlobalParameters.DEBUG, Boolean.toString(log.isDebugEnabled()));
        directServletConfig.put(
            GlobalParameters.JSON_REQUEST_PROCESSOR_THREAD_CLASS, ExtDirectJsonRequestProcessorThread.class.getName()
        );
        directServletConfig.put(
            GlobalParameters.GSON_BUILDER_CONFIGURATOR_CLASS, ExtDirectGsonBuilderConfigurator.class.getName()
        );

        serve(MOUNT_POINT + "*").with(ExtDirectServlet.class, directServletConfig);
        filter(MOUNT_POINT + "*").through(SecurityFilter.class);
      }
    });
  }
}
