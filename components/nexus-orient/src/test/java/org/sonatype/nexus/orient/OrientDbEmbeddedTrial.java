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
package org.sonatype.nexus.orient;

import java.io.File;
import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerSecurityConfiguration;
import com.orientechnologies.orient.server.config.OServerStorageConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Trials of using OrientDB embedded.
 */
@Ignore("Disable for now, as these cause other tests which do not explicitly setup embedding to fail")
public class OrientDbEmbeddedTrial
    extends TestSupport
{
  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }

  private ODocument createPerson(final ODatabaseDocumentTx db) {
    ODocument doc = db.newInstance("Person");
    doc.field("name", "Luke");
    doc.field("surname", "Skywalker");
    doc.field("city", new ODocument("City")
        .field("name", "Rome")
        .field("country", "Italy"));
    doc.save();
    return doc;
  }

  @Test
  public void embeddedServerFromXml() throws Exception {
    OServer server = OServerMain.create();
    URL config = getClass().getResource("server-config.xml");
    log(config);
    server.startup(config.openStream());
    server.activate();
    server.shutdown();
  }

  /**
   * Tests configuring the server w/o xml but configuring the JAXB objects directly.
   */
  @Test
  public void embeddedServerProgrammatic() throws Exception {
    File homeDir = util.createTempDir("orientdb-home").getCanonicalFile();
    System.setProperty(Orient.ORIENTDB_HOME, homeDir.getPath());

    OServer server = new OServer();
    OServerConfiguration config = new OServerConfiguration();

    File databaseDir = new File(homeDir, "db");
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

    // Dumps to System.out :-(
    OGlobalConfiguration.ENVIRONMENT_DUMP_CFG_AT_STARTUP.setValue(true);

    server.startup(config);
    server.activate();
    server.shutdown();
  }

  @Test
  public void embeddedDatabaseAccess() throws Exception {
    OServer server = OServerMain.create();
    URL config = getClass().getResource("server-config.xml");
    log(config);
    server.startup(config.openStream());
    server.activate();

    // NOTE: This doesn't root into the databases directory, not sure why
    try {
      ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:test");
      if (!db.exists()) {
        db.create();
      }
      else {
        db.open("admin", "admin");
      }
      try {
        ODocument doc = createPerson(db);
        log(doc.getIdentity());
        log(doc.getVersion());
        log(doc);
      }
      finally {
        db.close();
      }
    }
    finally {
      server.shutdown();
    }
  }
}
