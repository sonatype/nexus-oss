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
package org.sonatype.nexus.groovyremote;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.gossip.support.DC;
import org.sonatype.nexus.threads.FakeAlmightySubject;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import groovyx.remote.server.Receiver;
import groovyx.remote.transport.http.RemoteControlHttpHandler;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.eclipse.sisu.BeanEntry;
import org.eclipse.sisu.EagerSingleton;
import org.eclipse.sisu.inject.BeanLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Groovy-remote control service.
 *
 * @since 2.6
 */
@Named
@EagerSingleton
@IgnoreJRERequirement
public class RemoteControl
    extends LifecycleSupport
{
  private static final String CPREFIX = "${nexus.groovyremote.";

  private final BeanLocator beanLocator;

  private final ClassLoader uberClassLoader;

  private final int port;

  private HttpServer server;

  private final AtomicInteger requests = new AtomicInteger(0);

  @Inject
  public RemoteControl(final BeanLocator beanLocator,
                       final @Named("nexus-uber") ClassLoader uberClassLoader,
                       final @Named(CPREFIX + "port:-0}") int port)
  {
    this.beanLocator = checkNotNull(beanLocator);
    this.uberClassLoader = checkNotNull(uberClassLoader);
    this.port = port;
    log.info("Port: {}", port);
  }

  //
  // TODO: Consider using RemoteControlServlet implementation instead, and hooking up to NX proper w/security?
  // TODO: ... then this plugin could be used more generally to script Nexus.
  // TODO: May need both actually, as this method is defs easier for testing as it side-steps the rest of the NX abstractions here.
  //

  @Override
  protected void doStart() throws Exception {
    InetSocketAddress addr = new InetSocketAddress(port);
    this.server = HttpServer.create(addr, 0);

    Receiver receiver = new ReceiverImpl(uberClassLoader, createContext());
    final RemoteControlHttpHandler handler = new RemoteControlHttpHandler(receiver);

    server.createContext("/", new HttpHandler()
    {
      @Override
      public void handle(final HttpExchange exchange) throws IOException {
        int id = requests.incrementAndGet();
        log.debug("Handling remote request: [{}] {}", id, exchange);

        DC.put(RemoteScript.class, id);

        try {
          handler.handle(exchange);
        }
        catch (Throwable failure) {
          // HACK: Ignore this package-private exception as it seems to happen normally
          if ("sun.net.httpserver.StreamClosedException".equals(failure.getClass().getCanonicalName())) {
            log.trace("Ignoring", failure);
            return;
          }

          log.error("Handle failed", failure);
          Throwables.propagateIfPossible(failure, IOException.class);
          throw Throwables.propagate(failure);
        }
        finally {
          log.debug("Request finished");

          DC.remove(RemoteScript.class);
        }
      }
    });

    server.start();

    log.info("Listening: {}", server.getAddress());
  }

  /**
   * Ancillary marker for remote-script things (logging, DC, etc).
   */
  private static interface RemoteScript
  {
    // empty
  }

  /**
   * Container helpers for executing closure.
   */
  private final class ContainerHelper
  {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public <T> T lookup(final Key<T> key) {
      log.debug("Lookup: {}", key);
      final Iterator<? extends Entry<Annotation, T>> i = beanLocator.locate(key).iterator();
      return i.hasNext() ? i.next().getValue() : null;
    }

    public <T> T lookup(final Class<T> type) {
      return lookup(Key.get(type));
    }

    public <T> T lookup(final Class<T> type, final String name) {
      return lookup(type, Names.named(name));
    }

    public <T> T lookup(final Class<T> type, final Class<? extends Annotation> qualifier) {
      return lookup(Key.get(type, qualifier));
    }

    public <T> T lookup(final Class<T> type, final Annotation qualifier) {
      return lookup(Key.get(type, qualifier));
    }

    // String type-name helpers to avoid needing const class ref in closure

    public Class<?> type(final String typeName) throws ClassNotFoundException {
      log.debug("Lookup type: {}", typeName);
      return uberClassLoader.loadClass(typeName);
    }

    public Object lookup(final String typeName) throws ClassNotFoundException {
      return lookup(type(typeName));
    }

    public Object lookup(final String typeName, final String name) throws ClassNotFoundException {
      return lookup(type(typeName), name);
    }

    public <Q extends Annotation, T> Iterable<? extends BeanEntry<Q, T>> locate(final Key<T> key) {
      log.debug("Locate: {}", key);
      return beanLocator.locate(key);
    }

    public <Q extends Annotation, T> Iterable<? extends BeanEntry<Q, T>> locate(final Class<T> type) {
      log.debug("Locate: {}", type);
      return beanLocator.locate(Key.get(type));
    }
  }

  /**
   * Security helpers for executing closure.
   */
  private final class SecurityHelper
  {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get the current subject.
     */
    public Subject getSubject() {
      return SecurityUtils.getSubject();
    }

    /**
     * Login a user.
     */
    public void login(final String username, final String password) {
      log.debug("Login; username: {}, password: {}", username, password);
      getSubject().login(new UsernamePasswordToken(username, password));
    }

    /**
     * Logout user.
     */
    public void logout() {
      log.debug("Logout");
      getSubject().logout();
    }

    /**
     * Run a {@link Callable} as a specific user.
     */
    public Object asUser(final String username, final String password, final Callable callable) throws Exception {
      log.debug("Running as user: {}", username);
      login(username, password);
      try {
        return callable.call();
      }
      finally {
        logout();
      }
    }

    /**
     * Run a {@link Callable} as system user.
     */
    public Object asSystem(final Callable callable) {
      log.debug("Running as system");
      return FakeAlmightySubject.forUserId("*SYSTEM").execute(callable);
    }
  }

  /**
   * Creates the context of the executing closure.
   */
  private Map<String, Object> createContext() {
    Map<String, Object> context = Maps.newHashMap();
    context.put("container", new ContainerHelper());
    context.put("security", new SecurityHelper());
    context.put("logger", LoggerFactory.getLogger(RemoteScript.class));
    context.put("classLoader", uberClassLoader);

    // TODO: Expose json marshalling so we can get back objects that aren't serializable w/o having to rebuild structures around them
    // TODO: Make some simple closure helpers: log() and json() ?

    return context;
  }

  @Override
  protected void doStop() throws Exception {
    server.stop(0);
    server = null;
  }
}
