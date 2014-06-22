package org.sonatype.nexus.db;

import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
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
  public void embeddedServer() throws Exception {
    OServer server = OServerMain.create();
    URL config = getClass().getResource("server-config.xml");
    log(config);
    server.startup(config.openStream());
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
