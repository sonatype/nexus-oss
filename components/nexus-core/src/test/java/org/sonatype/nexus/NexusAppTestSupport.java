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
package org.sonatype.nexus;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Properties;

import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.events.EventSubscriberHost;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.maven.routing.Config;
import org.sonatype.nexus.proxy.maven.routing.internal.ConfigImpl;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.security.TestSecurityModule;
import org.sonatype.nexus.security.subject.FakeAlmightySubject;
import org.sonatype.nexus.templates.TemplateManager;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.test.NexusTestSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;

import static org.junit.Assert.fail;

public abstract class NexusAppTestSupport
    extends NexusTestSupport
{
  public static final String PROXY_SERVER_PORT = "proxy.server.port";

  private TaskScheduler nexusScheduler;

  private EventSubscriberHost eventSubscriberHost;

  private EventBus eventBus;

  private ApplicationConfiguration nexusConfiguration;

  private TemplateManager templateManager;

  protected boolean runWithSecurityDisabled() {
    return true;
  }

  protected boolean shouldLoadConfigurationOnStartup() {
    return false;
  }

  // NxApplication

  private boolean nexusStarted = false;

  /**
   * Preferred way to "boot" Nexus in unit tests. Previously, UTs were littered with code like this:
   *
   * <pre>
   * lookup(Nexus.class); // boot nexus
   * </pre>
   *
   * This was usually in {@link #setUp()} method override, and then another override was made in {@link #tearDown()}.
   * Using this method you don't have to fiddle with "shutdown" anymore, and also, you can invoke it in some prepare
   * method (like setUp) but also from test at any place. You have to ensure this method is not called multiple times,
   * as that signals a bad test (start nexus twice?), and exception will be thrown.
   */
  protected void startNx() throws Exception {
    if (nexusStarted) {
      throw new IllegalStateException("Bad test, as startNx was already invoked once!");
    }
    lookup(NxApplication.class).start();
    nexusStarted = true;
  }

  /**
   * Shutdown Nexus if started.
   */
  @After
  public void stopNx() throws Exception {
    if (nexusStarted) {
      lookup(NxApplication.class).stop();
    }
  }

  /**
   * Returns true if startNx method was invoked, if Nexus was started.
   */
  protected boolean isNexusStarted() {
    return nexusStarted;
  }

  // NxApplication

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);
    modules.add(new TestSecurityModule());
    modules.add(new Module()
    {
      @Override
      public void configure(final Binder binder) {
        binder.bind(Config.class).toInstance(new ConfigImpl(enableAutomaticRoutingFeature()));
      }
    });
  }

  @Override
  protected void customizeProperties(Properties ctx) {
    super.customizeProperties(ctx);
    ctx.put(PROXY_SERVER_PORT, String.valueOf(allocatePort()));
  }

  private int allocatePort() {
    ServerSocket ss;
    try {
      ss = new ServerSocket(0);
    }
    catch (IOException e) {
      return 0;
    }
    int port = ss.getLocalPort();
    try {
      ss.close();
    }
    catch (IOException e) {
      // does it matter?
      fail("Error allocating port " + e.getMessage());
    }
    return port;
  }

  protected boolean enableAutomaticRoutingFeature() {
    return false;
  }

  @Override
  protected void setUp() throws Exception {
    // remove Shiro thread locals, as things like DelegatingSubjects might lead us to old instance of SM
    ThreadContext.remove();
    super.setUp();

    eventBus = lookup(EventBus.class);
    nexusScheduler = lookup(TaskScheduler.class);
    eventSubscriberHost = lookup(EventSubscriberHost.class);
    nexusConfiguration = lookup(ApplicationConfiguration.class);
    templateManager = lookup(TemplateManager.class);

    if (shouldLoadConfigurationOnStartup()) {
      loadConfiguration();
    }

    if (runWithSecurityDisabled()) {
      shutDownSecurity();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    // FIXME: This needs to be fired as many component relies on this to cleanup (like EHCache)
    if (eventBus != null) {
      eventBus.post(new NexusStoppedEvent(null));
    }
    waitForTasksToStop();
    super.tearDown();
    // remove Shiro thread locals, as things like DelegatingSubjects might lead us to old instance of SM
    ThreadContext.remove();
  }

  protected EventBus eventBus() {
    return eventBus;
  }

  protected ApplicationConfiguration nexusConfiguration() {
    return nexusConfiguration;
  }

  protected TemplateSet getRepositoryTemplates() {
    return templateManager.getTemplates().getTemplates(RepositoryTemplate.class);
  }

  protected void shutDownSecurity() throws Exception {
    System.out.println("== Shutting down SECURITY!");

    loadConfiguration();

    ThreadContext.bind(FakeAlmightySubject.forUserId("disabled-security"));

    System.out.println("== Shutting down SECURITY!");
  }

  protected void loadConfiguration() throws IOException {
    nexusConfiguration.loadConfiguration(false);
    nexusConfiguration.saveConfiguration();
  }

  protected void killActiveTasks() throws Exception {
    nexusScheduler.killAll();
  }

  protected void waitForAsyncEventsToCalmDown() throws Exception {
    while (!eventSubscriberHost.isCalmPeriod()) {
      Thread.sleep(100);
    }
  }

  protected void waitForTasksToStop() throws Exception {
    if (nexusScheduler == null) {
      return;
    }

    // Give task a chance to start
    Thread.sleep(100);
    Thread.yield();

    int counter = 0;

    while (nexusScheduler.getRunningTaskCount() > 0) {
      Thread.sleep(100);
      counter++;

      if (counter > 300) {
        System.out.println("TIMEOUT WAITING FOR TASKS TO COMPLETE!!!  Will kill them.");
        killActiveTasks();
        break;
      }
    }
  }
}
