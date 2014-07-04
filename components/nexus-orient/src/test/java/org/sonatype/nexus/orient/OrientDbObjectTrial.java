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

import javax.persistence.Id;
import javax.persistence.Version;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Trials of using OrientDB object-api.
 */
public class OrientDbObjectTrial
    extends TestSupport
{
  static {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
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

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "id=" + id +
          ", version=" + version +
          ", firstName='" + firstName + '\'' +
          ", lastName='" + lastName + '\'' +
          '}';
    }
  }

  @Test
  public void objectTx() throws Exception {
    try (OObjectDatabaseTx db = new OObjectDatabaseTx("memory:test").create()) {
      db.getEntityManager().registerEntityClass(Person.class);
      Person newPerson = db.newInstance(Person.class);
      newPerson.setFirstName("James");
      newPerson.setLastName("Bond");
      db.save(newPerson);

      for (Person person : db.browseClass(Person.class)) {
        log("{}v{} -> {} {}", person.getId(), person.getVersion(), person.getFirstName(), person.getLastName());

        // NOTE: The javaassist proxy here doesn't properly toString(), need to have detached object for that to work??
        log("toString: {}", person);
        log("detached toString: {}", db.detach(person, true));
      }

      db.drop();
    }
  }
}
