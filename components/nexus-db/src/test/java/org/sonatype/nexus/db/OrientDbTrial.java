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
  public void embeddedServer() throws Exception {
    OServer server = OServerMain.create();
    URL config = getClass().getResource("server-config.xml");
    log(config);
    server.startup(config.openStream());
    server.activate();
    server.shutdown();
  }

  //@Test
  //public void embeddedDatabaseAccess() throws Exception {
  //  OServer server = OServerMain.create();
  //  URL config = getClass().getResource("server-config.xml");
  //  log(config);
  //  server.startup(config.openStream());
  //  server.activate();
  //
  //  try {
  //    ODatabaseDocumentTx db = (ODatabaseDocumentTx) server.openDatabase("document", "test", null, null);
  //    try {
  //      ODocument doc = db.newInstance("Test");
  //      doc.field("foo", "bar");
  //      doc.save();
  //    }
  //    finally {
  //      db.close();
  //    }
  //  }
  //  finally {
  //    server.shutdown();
  //  }
  //}

  @Test
  public void documentTx() throws Exception {
    File dir = util.createTempDir("testdb");
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath()).create();
    try {
      ODocument doc = db.newInstance("Person");
      doc.field("name", "Luke");
      doc.field("surname", "Skywalker");
      doc.field("city", new ODocument("City")
          .field("name", "Rome")
          .field("country", "Italy"));
      doc.save();
    }
    finally {
      db.close();
    }
  }

  public static class Person
  {
    private String firstName;

    private String lastName;

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(final String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(final String lastName) {
      this.lastName = lastName;
    }
  }

  @Test
  public void objectTx() throws Exception {
    File dir = util.createTempDir("testdb");
    OObjectDatabaseTx db = new OObjectDatabaseTx("plocal:" + dir.getPath()).create();
    try {
      db.getEntityManager().registerEntityClass(Person.class);
      Person newPerson = db.newInstance(Person.class);
      newPerson.setFirstName("James");
      newPerson.setLastName("Bond");
      db.save(newPerson);

      for (Person person : db.browseClass(Person.class)) {
        // NOTE: The javaassist proxy here doesn't properly toString()
        log("{} {}", person.getFirstName(), person.getLastName());
      }
    }
    finally {
      db.close();
    }
  }
}
