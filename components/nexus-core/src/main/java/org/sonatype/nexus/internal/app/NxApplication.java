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
package org.sonatype.nexus.internal.app;

import java.lang.management.ManagementFactory;

import javax.crypto.Cipher;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.ApplicationStatusSource;
import org.sonatype.nexus.common.app.NexusInitializedEvent;
import org.sonatype.nexus.common.app.NexusStartedEvent;
import org.sonatype.nexus.common.app.NexusStoppedEvent;
import org.sonatype.nexus.common.app.NexusStoppingEvent;
import org.sonatype.nexus.common.app.SystemState;
import org.sonatype.nexus.common.event.EventSubscriberHost;
import org.sonatype.nexus.internal.orient.OrientBootstrap;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import org.eclipse.sisu.bean.BeanManager;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import static com.google.common.base.Preconditions.checkNotNull;

// NOTE: component name needed for lookup by nexus-extender components.

/**
 * Boots Nexus application and critical components.
 *
 * @since 2.7
 */
@Named("NxApplication")
@Singleton
public class NxApplication
    extends LifecycleSupport
{
  private final EventBus eventBus;

  private final ApplicationStatusSource applicationStatusSource;

  private final SecuritySystem securitySystem;

  private final EventSubscriberHost eventSubscriberHost;

  private final OrientBootstrap orientBootstrap;

  private final BeanManager beanManager;

  @Inject
  public NxApplication(final EventBus eventBus,
                       final ApplicationStatusSource applicationStatusSource,
                       final SecuritySystem securitySystem,
                       final EventSubscriberHost eventSubscriberHost,
                       final OrientBootstrap orientBootstrap,
                       final BeanManager beanManager)
  {
    this.eventBus = checkNotNull(eventBus);
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.securitySystem = checkNotNull(securitySystem);
    this.eventSubscriberHost = checkNotNull(eventSubscriberHost);
    this.orientBootstrap = checkNotNull(orientBootstrap);
    this.beanManager = checkNotNull(beanManager);

    logInitialized();
  }

  private void logInitialized() {
    StringBuilder buff = new StringBuilder();
    buff.append("\n-------------------------------------------------\n\n");
    buff.append("Initializing ").append(getNexusNameForLogs());
    buff.append("\n\n-------------------------------------------------");
    log.info(buff.toString());
  }

  private String getNexusNameForLogs() {
    StringBuilder buff = new StringBuilder();
    buff.append("Sonatype Nexus ").append(applicationStatusSource.getSystemStatus().getEditionShort());
    buff.append(" ").append(applicationStatusSource.getSystemStatus().getVersion());
    return buff.toString();
  }

  @Override
  protected void doStart() throws Exception {
    if (Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE) {
      log.info("Unlimited strength JCE policy detected");
    }

    // register core and plugin contributed subscribers, start dispatching events to them
    eventSubscriberHost.start();

    applicationStatusSource.setState(SystemState.STOPPED);

    // HACK: Must start database services manually
    orientBootstrap.start();

    eventBus.post(new NexusInitializedEvent(this));

    applicationStatusSource.getSystemStatus().setState(SystemState.STARTING);

    securitySystem.start();

    applicationStatusSource.getSystemStatus().setState(SystemState.STARTED);

    log.info("Started {}", getNexusNameForLogs());
    eventBus.post(new NexusStartedEvent(this));
  }

  @Override
  protected void doStop() throws Exception {
    applicationStatusSource.getSystemStatus().setState(SystemState.STOPPING);

    // Due to no dependency mechanism in NX for components, we need to fire off a hint about shutdown first
    eventBus.post(new NexusStoppingEvent(this));

    // kill services + notify
    eventBus.post(new NexusStoppedEvent(this));
    eventSubscriberHost.stop();

    securitySystem.stop();

    // HACK: Must stop database services manually
    orientBootstrap.stop();

    // dispose of JSR-250
    beanManager.unmanage();

    applicationStatusSource.getSystemStatus().setState(SystemState.STOPPED);

    long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
    log.info("Uptime: {}", PeriodFormat.getDefault().print(new Period(uptime)));
    log.info("Stopped {}", getNexusNameForLogs());
  }
}
