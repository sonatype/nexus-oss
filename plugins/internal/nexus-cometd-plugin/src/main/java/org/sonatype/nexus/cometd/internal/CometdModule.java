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

package org.sonatype.nexus.cometd.internal;

import java.util.Map;

import javax.inject.Named;

import org.sonatype.nexus.web.MdcUserContextFilter;
import org.sonatype.security.web.guice.SecurityWebFilter;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;
import org.cometd.websocket.server.WebSocketTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CometD Guice module.
 *
 * @since 2.7
 */
@Named
public class CometdModule
    extends AbstractModule
{
  private static final Logger log = LoggerFactory.getLogger(CometdModule.class);

  private static final String MOUNT_POINT = "/service/cometd";

  @Override
  protected void configure() {
    bind(SecurityWebFilter.class);

    install(new ServletModule()
    {
      @Override
      protected void configureServlets() {
        Map<String, String> params = ImmutableMap.of(
            "transports", WebSocketTransport.class.getName()
        );
        serve(MOUNT_POINT + "/*").with(CometdServletImpl.class, params);
        filter(MOUNT_POINT + "/*").through(SecurityWebFilter.class);
        filter(MOUNT_POINT + "/*").through(MdcUserContextFilter.class);
      }
    });

    // FIXME: This has problems due to defaulting to have a no-session RealmSecurityManager ( see NexusWebModule ).
    //install(new FilterChainModule()
    //{
    //  @Override
    //  protected void configure() {
    //    addFilterChain(MOUNT_POINT + "/**", "authcNxBasic");
    //  }
    //});

    log.info("CometD support configured: {}", MOUNT_POINT);
  }
}
