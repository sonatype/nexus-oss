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

package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.plugins.PluginManagerResponse;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link NxApplication}.
 */
@Singleton
@Named
public class NxApplicationImpl
    extends LifecycleSupport
    implements NxApplication
{
  private final EventBus eventBus;

  private final ApplicationStatusSource applicationStatusSource;

  private final NexusConfiguration nexusConfiguration;

  private final NexusPluginManager nexusPluginManager;

  private final SecuritySystem securitySystem;

  private final NexusScheduler nexusScheduler;

  private final RepositoryRegistry repositoryRegistry;

  @Inject
  public NxApplicationImpl(final EventBus eventBus, final NexusConfiguration nexusConfiguration,
      final NexusPluginManager nexusPluginManager, final ApplicationStatusSource applicationStatusSource,
      final SecuritySystem securitySystem, final NexusScheduler nexusScheduler,
      final RepositoryRegistry repositoryRegistry)
  {
    this.eventBus = checkNotNull(eventBus);
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.nexusConfiguration = checkNotNull(nexusConfiguration);
    this.nexusPluginManager = checkNotNull(nexusPluginManager);
    this.securitySystem = checkNotNull(securitySystem);
    this.nexusScheduler = checkNotNull(nexusScheduler);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);

    logInitialized();

    log.info("Activating locally installed plugins...");
    final Collection<PluginManagerResponse> activationResponse = this.nexusPluginManager.activateInstalledPlugins();
    for (PluginManagerResponse response : activationResponse) {
      if (response.isSuccessful()) {
        log.info(response.formatAsString(log.isDebugEnabled()));
      }
      else {
        log.warn(response.formatAsString(log.isDebugEnabled()));
      }
    }

    applicationStatusSource.setState(SystemState.STOPPED);
    applicationStatusSource.getSystemStatus().setOperationMode(OperationMode.STANDALONE);
    applicationStatusSource.getSystemStatus().setInitializedAt(new Date());
    eventBus.post(new NexusInitializedEvent(this));
  }

  @VisibleForTesting
  protected void logInitialized() {
    final StringBuilder sysInfoLog = new StringBuilder();
    sysInfoLog.append("\n-------------------------------------------------\n\n");
    sysInfoLog.append("Initializing ").append(getNexusNameForLogs());
    sysInfoLog.append("\n\n-------------------------------------------------");
    log.info(sysInfoLog.toString());
  }

  @VisibleForTesting
  protected final String getNexusNameForLogs() {
    final StringBuilder msg = new StringBuilder();
    msg.append(applicationStatusSource.getSystemStatus().getAppName());
    msg.append(" ").append(applicationStatusSource.getSystemStatus().getVersion());
    return msg.toString();
  }

  @Override
  protected void doStart() {
    applicationStatusSource.getSystemStatus().setState(SystemState.STARTING);
    try {
      // force configuration load, validation and probable upgrade if needed
      // applies configuration and notifies listeners
      nexusConfiguration.loadConfiguration(true);
      // essential services
      securitySystem.start();
      securitySystem.getAnonymousUsername();
      nexusConfiguration.createInternals();
      nexusScheduler.initializeTasks();

      // notify about start other components participating in configuration framework
      eventBus.post(new ConfigurationChangeEvent(nexusConfiguration, null, null));

      applicationStatusSource.getSystemStatus().setLastConfigChange(new Date());
      applicationStatusSource.getSystemStatus().setFirstStart(nexusConfiguration.isConfigurationDefaulted());
      applicationStatusSource.getSystemStatus().setInstanceUpgraded(nexusConfiguration.isInstanceUpgraded());
      applicationStatusSource.getSystemStatus().setConfigurationUpgraded(nexusConfiguration.isConfigurationUpgraded());
      if (applicationStatusSource.getSystemStatus().isFirstStart()) {
        log.info("This is 1st start of new Nexus instance.");
      }
      if (applicationStatusSource.getSystemStatus().isInstanceUpgraded()) {
        log.info("This is an upgraded instance of Nexus.");
      }

      applicationStatusSource.getSystemStatus().setState(SystemState.STARTED);
      applicationStatusSource.getSystemStatus().setStartedAt(new Date());

      synchronizeShadowsAtStartup();

      if (log.isInfoEnabled()) {
        final File workDir = nexusConfiguration.getWorkingDirectory();
        String workDirPath = null;
        if (workDir != null) {
          try {
            workDirPath = workDir.getCanonicalPath();
          }
          catch (IOException ioe) {
            workDirPath = workDir.getAbsolutePath();
          }
        }
        log.info("Nexus Work Directory : {}", workDirPath);
        log.info("Started {}", getNexusNameForLogs());
      }
      eventBus.post(new NexusStartedEvent(this));
    }
    catch (IOException e) {
      applicationStatusSource.getSystemStatus().setState(SystemState.BROKEN_IO);
      applicationStatusSource.getSystemStatus().setErrorCause(e);
      log.error("Could not start Nexus, bad IO exception!", e);
      Throwables.propagate(e);
    }
    catch (ConfigurationException e) {
      applicationStatusSource.getSystemStatus().setState(SystemState.BROKEN_CONFIGURATION);
      applicationStatusSource.getSystemStatus().setErrorCause(e);
      log.error("Could not start Nexus, user configuration exception!", e);
      Throwables.propagate(e);
    }
  }

  @Override
  protected void doStop() {
    applicationStatusSource.getSystemStatus().setState(SystemState.STOPPING);
    // Due to no dependency mechanism in NX for components, we need to fire off a hint about shutdown first
    eventBus.post(new NexusStoppingEvent(this));
    // kill services + notify
    nexusScheduler.shutdown();
    eventBus.post(new NexusStoppedEvent(this));
    nexusConfiguration.dropInternals();
    securitySystem.stop();
    applicationStatusSource.getSystemStatus().setState(SystemState.STOPPED);
    log.info("Stopped {}", getNexusNameForLogs());
  }

  private void synchronizeShadowsAtStartup() {
    final Collection<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet(ShadowRepository.class);
    for (ShadowRepository shadow : shadows) {
      if (shadow.isSynchronizeAtStartup()) {
        final SynchronizeShadowsTask task = nexusScheduler.createTaskInstance(SynchronizeShadowsTask.class);
        task.setShadowRepositoryId(shadow.getId());
        nexusScheduler.submit("Shadow Sync (" + shadow.getId() + ")", task);
      }
    }
  }
}
