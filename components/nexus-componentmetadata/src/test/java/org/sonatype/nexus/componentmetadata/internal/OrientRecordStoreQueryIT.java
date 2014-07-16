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
package org.sonatype.nexus.componentmetadata.internal;

import java.util.List;

import org.sonatype.nexus.componentmetadata.Record;
import org.sonatype.nexus.componentmetadata.RecordType;
import org.sonatype.nexus.componentmetadata.RecordQuery;
import org.sonatype.nexus.componentmetadata.RecordStoreSession;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OrientRecordStoreQueryIT
    extends OrientRecordStoreITSupport
{
  // find

  @Test(expected = IllegalStateException.class)
  public void findBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    session.getSchema().addType(RC_GOOD_EMPTY);
    session.close();
    session.find(new RecordQuery(RC_GOOD_EMPTY));
  }

  @Test(expected = NullPointerException.class)
  public void findBadQueryNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      session.find(null);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadClassNotFound() {
    try (RecordStoreSession session = store.openSession()) {
      session.find(new RecordQuery(RC_GOOD_EMPTY));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void findBadClassDifferentDefinition() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      session.find(new RecordQuery(new RecordType(RC_GOOD_EMPTY.getName()).withStrict(true)));
    }
  }

  @Test
  public void findGoodNoFilter() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      Record record = session.create(RC_GOOD_EMPTY).set("stringField", stringValue).save();
      List<Record> result = session.find(new RecordQuery(RC_GOOD_EMPTY));
      assertThat(result.size(), is(1));
      assertThat(result.get(0), is(record));
    }
  }

  @Test
  public void findGoodWithFilterMatching() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      Record record = session.create(RC_GOOD_EMPTY).set("stringField", stringValue).save();
      List<Record> result = session.find(new RecordQuery(RC_GOOD_EMPTY).withEqual("stringField", stringValue));
      assertThat(result.size(), is(1));
      assertThat(result.get(0), is(record));
    }
  }

  @Test
  public void findGoodWithFilterNotMatching() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      Record record = session.create(RC_GOOD_EMPTY).set("stringField", stringValue).save();
      List<Record> result = session.find(new RecordQuery(RC_GOOD_EMPTY).withEqual("stringField", "not " + stringValue));
      assertThat(result.size(), is(0));
    }
  }

  // count

  @Test(expected = IllegalStateException.class)
  public void countBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    session.getSchema().addType(RC_GOOD_EMPTY);
    session.close();
    session.find(new RecordQuery(RC_GOOD_EMPTY));
  }

  @Test(expected = NullPointerException.class)
  public void countBadQueryNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      session.find(null);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void countBadClassNotFound() {
    try (RecordStoreSession session = store.openSession()) {
      session.find(new RecordQuery(RC_GOOD_EMPTY));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void countBadClassDifferentDefinition() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      session.find(new RecordQuery(new RecordType(RC_GOOD_EMPTY.getName()).withStrict(true)));
    }
  }

  @Test
  public void countGoodNoFilter() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      Record record = session.create(RC_GOOD_EMPTY).set("stringField", stringValue).save();
      assertThat(session.count(new RecordQuery(RC_GOOD_EMPTY)), is(1L));
    }
  }

  @Test
  public void countGoodWithFilterMatching() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      Record record = session.create(RC_GOOD_EMPTY).set("stringField", stringValue).save();
      assertThat(session.count(new RecordQuery(RC_GOOD_EMPTY).withEqual("stringField", stringValue)), is(1L));
    }
  }

  @Test
  public void countGoodWithFilterNotMatching() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      Record record = session.create(RC_GOOD_EMPTY).set("stringField", stringValue).save();
      assertThat(session.count(new RecordQuery(RC_GOOD_EMPTY).withEqual("stringField", "not " + stringValue)), is(0L));
    }
  }
}