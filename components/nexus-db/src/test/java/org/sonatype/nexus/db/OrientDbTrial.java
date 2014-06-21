package org.sonatype.nexus.db;

import java.io.File;
import java.net.URL;

import javax.persistence.Id;
import javax.persistence.Version;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import org.apache.shiro.codec.Hex;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

  @Test
  public void documentTx() throws Exception {
    File dir = util.createTempDir("testdb");
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath()).create();
    try {
      ODocument doc = createPerson(db);
    }
    finally {
      db.close();
    }
  }

  @Test
  public void globalPool() throws Exception {
    File dir = util.createTempDir("testdb");

    // first ensure the database is created, and close the connection
    new ODatabaseDocumentTx("plocal:" + dir.getPath()).create().close();

    // now we should be able to get a pooled connection
    ODatabaseDocumentTx db = ODatabaseDocumentPool.global().acquire("plocal:" + dir.getPath(), "admin", "admin");
    try {
      ODocument doc = createPerson(db);
    }
    finally {
      db.close();
    }
  }

  @Test
  public void recordIdEncoding() throws Exception {
    File dir = util.createTempDir("testdb");
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath()).create();
    try {
      ODocument doc = createPerson(db);
      log("New Document: {}", doc);

      ORID rid = doc.getIdentity();
      log("RID: {}", rid);

      String encoded = Hex.encodeToString(rid.toStream());
      log("Hex Encoded: {}", encoded);

      ORID decoded = new ORecordId().fromStream(Hex.decode(encoded));
      log("Decoded RID: {}", decoded);

      assertThat(decoded, is(rid));

      doc = db.getRecord(decoded);
      log("Fetched Document: {}", doc);
    }
    finally {
      db.close();
    }
  }

  @Test
  public void loadNonExistingDocument() throws Exception {
    File dir = util.createTempDir("testdb");
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath()).create();
    try {
      ORID rid = new ORecordId("#1:1");
      log(rid);
      assertThat(db.existsUserObjectByRID(rid), is(false));
      ORecordInternal record = db.load(rid); // or getRecord()
      log(record);
      // NOTE: load() and getRecord() return records for non-existing documents :-\
    }
    finally {
      db.close();
    }
  }

  @Test
  public void deleteNonExistingDocument() throws Exception {
    File dir = util.createTempDir("testdb");
    ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:" + dir.getPath()).create();
    try {
      ORID rid = new ORecordId("#1:1");
      log(rid);
      assertThat(db.existsUserObjectByRID(rid), is(false));

      db.delete(rid);
      // apparently continues w/o exception or other notification
    }
    finally {
      db.close();
    }
  }

  public static class Person
  {
    @Id
    private Object id;

    @Version
    private Object version;

    private String firstName;

    private String lastName;

    public Object getId() {
      return id;
    }

    public Object getVersion() {
      return version;
    }

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
        log("{}v{} -> {} {}", person.getId(), person.getVersion(), person.getFirstName(), person.getLastName());
      }
    }
    finally {
      db.close();
    }
  }
}
