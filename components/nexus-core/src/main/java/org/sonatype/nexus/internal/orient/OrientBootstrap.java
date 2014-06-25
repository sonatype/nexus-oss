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
package org.sonatype.nexus.internal.orient;

import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;
import org.sonatype.sisu.goodies.lifecycle.Starter;
import org.sonatype.sisu.goodies.lifecycle.Stopper;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.OConstants;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerSecurityConfiguration;
import com.orientechnologies.orient.server.config.OServerStorageConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;
import org.apache.commons.io.output.WriterOutputStream;
import org.eclipse.sisu.EagerSingleton;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * OrientDB bootstrap lifecycle adapter.
 * 
 * @since 3.0
 */
@Named
@EagerSingleton
public class OrientBootstrap
    extends LifecycleSupport
    implements EventSubscriber
{
  private final ApplicationDirectories applicationDirectories;

  private OServer server;

  @Inject
  public OrientBootstrap(final ApplicationDirectories applicationDirectories) {
    this.applicationDirectories = checkNotNull(applicationDirectories);

    log.info("OrientDB version: {}", OConstants.getVersion());

    // disable default shutdown-hook, will shutdown manually when nexus is stopped
    Orient.instance().removeShutdownHook();
  }

  @Override
  protected void doStart() throws Exception {
    OServer server = new OServer();
    OServerConfiguration config = createConfiguration();
    server.startup(config);

    // Log global configuration
    if (log.isDebugEnabled()) {
      StringWriter buff = new StringWriter();
      OGlobalConfiguration.dumpConfiguration(new PrintStream(new WriterOutputStream(buff), true));
      log.debug("Global configuration:\n{}", buff);
    }

    server.activate();
    this.server = server;
  }

  private OServerConfiguration createConfiguration() {
    // FIXME: Unsure what this directory us used for
    File homeDir = applicationDirectories.getWorkDirectory("orient");
    System.setProperty("orient.home", homeDir.getPath());
    System.setProperty(Orient.ORIENTDB_HOME, homeDir.getPath());

    OServerConfiguration config = new OServerConfiguration();

    // FIXME: Unsure what this is used for, its apparently assigned to xml location, but forcing it here
    config.location = "DYNAMIC-CONFIGURATION";

    File databaseDir = applicationDirectories.getWorkDirectory("db");
    config.properties = new OServerEntryConfiguration[] {
        new OServerEntryConfiguration("server.database.path", databaseDir.getPath())
    };

    config.handlers = Lists.newArrayList();

    config.hooks = Lists.newArrayList();

    config.network = new OServerNetworkConfiguration();
    config.network.protocols = Lists.newArrayList(
        new OServerNetworkProtocolConfiguration("binary", ONetworkProtocolBinary.class.getName())
    );

    OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
    binaryListener.ipAddress = "0.0.0.0";
    binaryListener.portRange = "2424-2430";
    binaryListener.protocol = "binary";
    binaryListener.socket = "default";

    config.network.listeners = Lists.newArrayList(
        binaryListener
    );

    config.storages = new OServerStorageConfiguration[] {};

    config.users = new OServerUserConfiguration[] {
        new OServerUserConfiguration("admin", "admin", "*")
    };

    config.security = new OServerSecurityConfiguration();
    config.security.users = Lists.newArrayList();
    config.security.resources = Lists.newArrayList();

    return config;
  }

  @Override
  protected void doStop() throws Exception {
    Orient.instance().shutdown();
    server = null;
  }

  //
  // Event adapters
  //

  @Subscribe
  public void on(final NexusInitializedEvent event) {
    Starter.start(this);
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) {
    Stopper.stop(this);
  }
}