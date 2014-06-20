package org.sonatype.nexus.db;

import java.io.File;
import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import org.junit.Test;

/**
 * Trials of using OrientDB.
 */
public class OrientDbTrial
    extends TestSupport
{
  @Test
  public void embeddedServerTest() throws Exception {
    OServer server = OServerMain.create();
    URL config = getClass().getResource("server-config.xml");
    log(config);
    server.startup(config.openStream());
    server.activate();
    server.shutdown();
  }

  @Test
  public void documentTxTest() throws Exception {
    File dir = util.createTempDir("testdb");
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath());
    if (!db.exists()) {
      db.create();
      log("DB created");
    }
    else {
      db.open("admin", "admin");
      log("DB opened");
    }

    try {
      ODocument doc = new ODocument("Person");
      doc.field("name", "Luke");
      doc.field("surname", "Skywalker");
      doc.field("city", new ODocument("City").field("name", "Rome").field("country", "Italy"));
      doc.save();
    }
    finally {
      db.close();
    }
  }
}
