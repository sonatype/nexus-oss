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
package org.sonatype.nexus.bootstrap.jetty;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.nexus.bootstrap.PropertyMap;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.sonatype.appcontext.internal.Preconditions.checkNotNull;

// NOTE: Based on org.eclipse.jetty.xml.XmlConfiguration#main()

/**
 * Jetty server.
 *
 * @since 2.8
 */
public class JettyServer
{
  private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

  private final ClassLoader classLoader;

  private final Map<String,String> properties;

  private final String[] args;

  private final List<LifeCycle> components = new ArrayList<>();

  public JettyServer(final ClassLoader classLoader, final Map<String,String> properties, final String[] args) {
    this.classLoader = checkNotNull(classLoader);
    this.properties = checkNotNull(properties);
    this.args = checkNotNull(args);
  }

  public synchronized void start() throws Exception {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      doStart();
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  private void doStart() throws Exception {
    if (!components.isEmpty()) {
      throw new IllegalStateException("Already started");
    }

    log.info("Starting");

    final AtomicReference<Throwable> exception = new AtomicReference<>();

    AccessController.doPrivileged(new PrivilegedAction<Object>()
    {
      public Object run() {
        try {
          PropertyMap props = new PropertyMap();
          props.putAll(JettyServer.this.properties);

          // For all arguments, load properties or parse XMLs
          XmlConfiguration last = null;
          Object[] obj = new Object[args.length];

          for (int i = 0; i < args.length; i++) {
            URL url = Resource.newResource(args[i]).getURL();

            if (url.getFile().toLowerCase(Locale.ENGLISH).endsWith(".properties")) {
              log.info("Loading properties: {}", url);

              props.load(url);
            }
            else {
              log.info("Applying configuration: {}", url);

              XmlConfiguration configuration = new XmlConfiguration(url);
              if (last != null) {
                configuration.getIdMap().putAll(last.getIdMap());
              }
              if (!props.isEmpty()) {
                configuration.getProperties().putAll(props);
              }
              obj[i] = configuration.configure();
              last = configuration;
            }
          }

          // For all objects created by XmlConfigurations, start them if they are lifecycles.
          for (int i = 0; i < args.length; i++) {
            if (obj[i] instanceof LifeCycle) {
              LifeCycle lc = (LifeCycle) obj[i];

              log.info("Starting component: {}", lc);
              components.add(lc);

              if (!lc.isRunning()) {
                lc.start();
              }
            }
          }
        }
        catch (Exception e) {
          exception.set(e);
        }
        return null;
      }
    });

    Throwable e = exception.get();
    if (e != null) {
      log.error("Failed to start components", e);

      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      else if (e instanceof Exception) {
        throw (Exception)e;
      }
      else if (e instanceof Error) {
        throw (Error)e;
      }
      throw new Error(e);
    }

    // complain if no components were started
    if (components.isEmpty()) {
      throw new Exception("Failed to start any components");
    }

    log.info("Started {} components", components.size());
  }

  public synchronized void stop() throws Exception {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      doStop();
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
    }
  }

  private void doStop() throws Exception {
    if (components.isEmpty()) {
      throw new IllegalStateException("Not started");
    }

    log.info("Stopping {} components", components.size());

    Collections.reverse(components);

    for (LifeCycle lc : components) {
      if (!lc.isRunning()) {
        log.info("Stopping component: {}", lc);
        lc.stop();
      }
    }

    components.clear();
  }
}
