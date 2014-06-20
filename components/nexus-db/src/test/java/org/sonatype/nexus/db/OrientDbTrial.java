package org.sonatype.nexus.db;

import java.io.File;
import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
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
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath()).create();
    try {
      ODocument doc = db.newInstance("Person");
      doc.field("name", "Luke");
      doc.field("surname", "Skywalker");
      doc.field("city", new ODocument("City").field("name", "Rome").field("country", "Italy"));
      doc.save();
    }
    finally {
      db.close();
    }
  }

  public static class Person
  {
    public String firstName;
    public String lastName;
  }

  @Test
  public void objectTxTest() throws Exception {
    File dir = util.createTempDir("testdb");
    OObjectDatabaseTx db = new OObjectDatabaseTx("plocal:" + dir.getPath()).create();
    db.getEntityManager().registerEntityClass(Person.class);
    try {
      Person person = new Person();
      person.firstName = "James";
      person.lastName = "Bond";
      db.save(person);
    }
    finally {
      db.close();
    }
  }
}
