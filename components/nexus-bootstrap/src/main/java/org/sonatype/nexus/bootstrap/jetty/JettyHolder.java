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
package org.sonatype.nexus.bootstrap.jetty;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.server.Server;

/**
 * Jetty holder.
 *
 * @since 2.8
 */
public class JettyHolder
{
  private static CountDownLatch holderSet = new CountDownLatch(1);

  private static Holder holder;

  public static Holder getHolder() {
    try {
      holderSet.await();
      holder.started.await();
      return holder;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void setHolder(final Holder h) {
    holder = h;
    holderSet.countDown();
  }

  public static class Holder
  {
    private final CountDownLatch started;

    private final Server server;

    private final Map<String, String> properties;

    private final Map<String, Object> namedBeans;

    public Holder(final CountDownLatch started,
                  final Server server,
                  final Map<String, String> properties,
                  final Map<String, Object> namedBeans)
    {
      this.started = started;
      this.server = server;
      this.properties = properties;
      this.namedBeans = namedBeans;
    }

    public Server getServer() {
      return server;
    }

    public Map<String, String> getProperties() {
      return properties;
    }

    public Map<String, Object> getNamedBeans() {
      return namedBeans;
    }
  }

}
