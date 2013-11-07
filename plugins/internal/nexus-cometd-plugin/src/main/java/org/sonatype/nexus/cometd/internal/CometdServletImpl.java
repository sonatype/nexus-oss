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

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.CometdServlet;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CometdServlet} extensions.
 *
 * @since 2.7
 */
@Named
@Singleton
public class CometdServletImpl
    extends CometdServlet
    implements Provider<BayeuxServer>
{
  private static final Logger log = LoggerFactory.getLogger(CometdServletImpl.class);

  // TODO: Adapt service registration via AnnotationCometdServlet

  // TODO: Register JMX mbeans

  @Override
  public void init() throws ServletException {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      super.init();
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  public void service(final ServletRequest request, final ServletResponse response)
      throws ServletException, IOException
  {
    log.debug("service");

    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      super.service(request, response);
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  @Override
  protected BayeuxServerImpl newBayeuxServer() {
    BayeuxServerImpl server = new BayeuxServerImpl();

    // TODO: Install security policy
    // TODO: See http://docs.cometd.org/reference/java_server.html#java_server_authorization
    // TODO: See http://cometd.org/documentation/2.x/howtos/authentication

    server.addExtension(new AcknowledgedMessagesExtension());
    return server;
  }

  @Override
  public BayeuxServer get() {
    return getBayeux();
  }
}
