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
package org.sonatype.nexus.internal.orient;

import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.ApplicationDirectories;
import org.sonatype.nexus.jmx.reflect.ManagedAttribute;
import org.sonatype.nexus.jmx.reflect.ManagedObject;
import org.sonatype.nexus.orient.DatabaseServer;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.Lists;
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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link DatabaseServer} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
@ManagedObject
public class DatabaseServerImpl
    extends LifecycleSupport
    implements DatabaseServer
{
  private final ApplicationDirectories applicationDirectories;

  private final boolean listenerEnabled;

  private OServer server;

  @Inject
  public DatabaseServerImpl(final ApplicationDirectories applicationDirectories,
                            final @Named("${nexus.orient.listenerEnabled:-false}") boolean listenerEnabled)
  {
    this.applicationDirectories = checkNotNull(applicationDirectories);
    this.listenerEnabled = listenerEnabled;

    log.info("OrientDB version: {}", OConstants.getVersion());

    // disable default global shutdown-hook, will shutdown manually when nexus is stopped
    Orient.instance().removeShutdownHook();
  }

  @ManagedAttribute
  public boolean isListenerEnabled() {
    return listenerEnabled;
  }

  @Override
  protected void doStart() throws Exception {
    // global startup
    Orient.instance().startup();

    // instance startup
    OServer server = new OServer();
    OServerConfiguration config = createConfiguration();
    server.startup(config);

    // create default root user to avoid orientdb prompt on console
    server.addUser(OServerConfiguration.SRV_ROOT_ADMIN, null, "*");

    // Log global configuration
    if (log.isDebugEnabled()) {
      StringWriter buff = new StringWriter();
      // FIXME: Remove need for commons-io WriterOutputStream
      OGlobalConfiguration.dumpConfiguration(new PrintStream(new WriterOutputStream(buff), true));
      log.debug("Global configuration:\n{}", buff);
    }

    server.activate();
    log.info("Activated");

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
    config.properties = new OServerEntryConfiguration[]{
        new OServerEntryConfiguration("server.database.path", databaseDir.getPath())
    };

    config.handlers = Lists.newArrayList();

    config.hooks = Lists.newArrayList();

    config.network = new OServerNetworkConfiguration();
    config.network.protocols = Lists.newArrayList(
        new OServerNetworkProtocolConfiguration("binary", ONetworkProtocolBinary.class.getName())
    );

    config.network.listeners = Lists.newArrayList();

    // HACK: Optionally enable the binary listener
    if (listenerEnabled) {
      OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
      binaryListener.ipAddress = "0.0.0.0";
      binaryListener.portRange = "2424-2430";
      binaryListener.protocol = "binary";
      binaryListener.socket = "default";
      config.network.listeners.add(binaryListener);
      log.info("Listener enabled: {}:[{}]", binaryListener.ipAddress, binaryListener.portRange);
    }

    config.storages = new OServerStorageConfiguration[]{};

    config.users = new OServerUserConfiguration[]{
        new OServerUserConfiguration("admin", "admin", "*")
    };

    config.security = new OServerSecurityConfiguration();
    config.security.users = Lists.newArrayList();
    config.security.resources = Lists.newArrayList();

    // latest advice is to disable DB compression as it doesn't buy much,
    // also snappy has issues with use of native lib (unpacked under tmp)
    OGlobalConfiguration.STORAGE_COMPRESSION_METHOD.setValue("nothing");

    return config;
  }

  @Override
  protected void doStop() throws Exception {
    // instance shutdown
    server.shutdown();
    server = null;

    // global shutdown
    Orient.instance().shutdown();

    log.info("Shutdown");
  }
}
