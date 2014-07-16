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

import java.math.BigDecimal;
import java.util.Date;

import org.sonatype.nexus.componentmetadata.Record;
import org.sonatype.nexus.componentmetadata.RecordType;
import org.sonatype.nexus.componentmetadata.RecordId;
import org.sonatype.nexus.componentmetadata.RecordStoreSession;
import org.sonatype.nexus.componentmetadata.RecordVersion;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class OrientRecordStoreCrudIT
    extends OrientRecordStoreITSupport
{
  // create

  @Test(expected = IllegalStateException.class)
  public void createBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    session.getSchema().addType(RC_GOOD_EMPTY);
    session.close();
    session.create(RC_GOOD_EMPTY);
  }

  @Test(expected = NullPointerException.class)
  public void createBadClassNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.create(null);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void createBadClassNotFound() {
    try (RecordStoreSession session = store.openSession()) {
      session.create(RC_GOOD_EMPTY);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void createBadClassAbstract() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_SUPERTYPE);
      session.create(RC_GOOD_SUPERTYPE);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void createBadClassDifferentDefinition() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);
      session.create(new RecordType(RC_GOOD_EMPTY.getName()).withStrict(true));
    }
  }

  @Test
  public void createSaveGetModifyGetDelete() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_EMPTY);

      // create a simple record first so we can point to it
      Record otherRecord = session.create(RC_GOOD_EMPTY);
      otherRecord.set("name", "other");
      otherRecord.save();

      Record record = session.create(RC_GOOD_EMPTY);

      // check initial state of record
      assertThat(record.getType(), is(RC_GOOD_EMPTY));
      assertThat(record.isPersistent(), is(false));
      assertGetIdThrowsIllegalStateException(record);
      assertGetVersionThrowsIllegalStateException(record);
      assertThat(record.has("recordField"), is(false));
      assertThat(record.has("recordIdField"), is(false));
      assertThat(record.has("bigDecimalField"), is(false));
      assertThat(record.has("byteField"), is(false));
      assertThat(record.has("dateField"), is(false));
      assertThat(record.has("doubleField"), is(false));
      assertThat(record.has("floatField"), is(false));
      assertThat(record.has("integerField"), is(false));
      assertThat(record.has("longField"), is(false));
      assertThat(record.has("shortField"), is(false));
      assertThat(record.has("stringField"), is(false));
      assertGetRecordThrowsNullPointerException(record, "recordField");
      assertGetThrowsNullPointerException(record, "bigDecimalField");
      assertGetThrowsNullPointerException(record, "byteField");
      assertGetThrowsNullPointerException(record, "dateField");
      assertGetThrowsNullPointerException(record, "doubleField");
      assertGetThrowsNullPointerException(record, "floatField");
      assertGetThrowsNullPointerException(record, "integerField");
      assertGetThrowsNullPointerException(record, "longField");
      assertGetThrowsNullPointerException(record, "shortField");
      assertGetThrowsNullPointerException(record, "stringField");
      assertThat(record.keySet().size(), is(0));

      // add fields, but don't save yet
      record.set("recordField", otherRecord);
      record.set("recordIdField", otherRecord.getId());
      record.set("bigDecimalField", bigDecimalValue);
      record.set("byteField", byteValue);
      record.set("dateField", dateValue);
      record.set("doubleField", doubleValue);
      record.set("floatField", floatValue);
      record.set("integerField", integerValue);
      record.set("longField", longValue);
      record.set("shortField", shortValue);
      record.set("stringField", stringValue);

      // check state again before saving
      assertThat(record.getType(), is(RC_GOOD_EMPTY));
      assertThat(record.isPersistent(), is(false));
      assertGetIdThrowsIllegalStateException(record);
      assertGetVersionThrowsIllegalStateException(record);
      assertThat(record.has("recordField"), is(true));
      assertThat(record.has("recordIdField"), is(true));
      assertThat(record.has("bigDecimalField"), is(true));
      assertThat(record.has("byteField"), is(true));
      assertThat(record.has("dateField"), is(true));
      assertThat(record.has("doubleField"), is(true));
      assertThat(record.has("floatField"), is(true));
      assertThat(record.has("integerField"), is(true));
      assertThat(record.has("longField"), is(true));
      assertThat(record.has("shortField"), is(true));
      assertThat(record.has("stringField"), is(true));
      assertThat(record.getRecord("recordField"), is(otherRecord));
      assertThat((BigDecimal) record.get("bigDecimalField"), is(bigDecimalValue));
      assertThat((Byte) record.get("byteField"), is(byteValue));
      assertThat((Date) record.get("dateField"), is(dateValue));
      assertThat((Double) record.get("doubleField"), is(doubleValue));
      assertThat((Float) record.get("floatField"), is(floatValue));
      assertThat((Integer) record.get("integerField"), is(integerValue));
      assertThat((Long) record.get("longField"), is(longValue));
      assertThat((Short) record.get("shortField"), is(shortValue));
      assertThat((String) record.get("stringField"), is(stringValue));
      assertThat(record.keySet().size(), is(11));

      // save, then check state again (same as before, but isPersistent is true and id and version are defined)
      record.save();
      assertThat(record.getType(), is(RC_GOOD_EMPTY));
      assertThat(record.isPersistent(), is(true));
      assertThat(record.getId().getValue().length() > 0, is(true));
      assertThat(record.getVersion().getValue().length() > 0, is(true));
      assertThat(record.has("recordField"), is(true));
      assertThat(record.has("recordIdField"), is(true));
      assertThat(record.has("bigDecimalField"), is(true));
      assertThat(record.has("byteField"), is(true));
      assertThat(record.has("dateField"), is(true));
      assertThat(record.has("doubleField"), is(true));
      assertThat(record.has("floatField"), is(true));
      assertThat(record.has("integerField"), is(true));
      assertThat(record.has("longField"), is(true));
      assertThat(record.has("shortField"), is(true));
      assertThat(record.has("stringField"), is(true));
      assertThat(record.getRecord("recordField"), is(otherRecord));
      assertThat((BigDecimal) record.get("bigDecimalField"), is(bigDecimalValue));
      assertThat((Byte) record.get("byteField"), is(byteValue));
      assertThat((Date) record.get("dateField"), is(dateValue));
      assertThat((Double) record.get("doubleField"), is(doubleValue));
      assertThat((Float) record.get("floatField"), is(floatValue));
      assertThat((Integer) record.get("integerField"), is(integerValue));
      assertThat((Long) record.get("longField"), is(longValue));
      assertThat((Short) record.get("shortField"), is(shortValue));
      assertThat((String) record.get("stringField"), is(stringValue));
      assertThat(record.keySet().size(), is(11));

      // get, then check state again (same as before)
      RecordId id = record.getId();
      RecordVersion version = record.getVersion();
      record = session.get(id);
      if (record == null) {
        fail("Record not found");
      }
      assertThat(record.getType(), is(RC_GOOD_EMPTY));
      assertThat(record.isPersistent(), is(true));
      assertThat(record.getId(), is(id));
      assertThat(record.getVersion(), is(version));
      assertThat(record.has("recordField"), is(true));
      assertThat(record.has("recordIdField"), is(true));
      assertThat(record.has("bigDecimalField"), is(true));
      assertThat(record.has("byteField"), is(true));
      assertThat(record.has("dateField"), is(true));
      assertThat(record.has("doubleField"), is(true));
      assertThat(record.has("floatField"), is(true));
      assertThat(record.has("integerField"), is(true));
      assertThat(record.has("longField"), is(true));
      assertThat(record.has("shortField"), is(true));
      assertThat(record.has("stringField"), is(true));
      assertThat(record.getRecord("recordField"), is(otherRecord));
      assertThat((BigDecimal) record.get("bigDecimalField"), is(bigDecimalValue));
      assertThat((Byte) record.get("byteField"), is(byteValue));
      assertThat((Date) record.get("dateField"), is(dateValue));
      assertThat((Double) record.get("doubleField"), is(doubleValue));
      assertThat((Float) record.get("floatField"), is(floatValue));
      assertThat((Integer) record.get("integerField"), is(integerValue));
      assertThat((Long) record.get("longField"), is(longValue));
      assertThat((Short) record.get("shortField"), is(shortValue));
      assertThat((String) record.get("stringField"), is(stringValue));
      assertThat(record.keySet().size(), is(11));

      // modify, save, get, then check state (same as before, but different version and stringValue)
      record.set("stringField", "newStringValue");
      record.save();
      record = session.get(id);
      if (record == null) {
        fail("Record not found");
      }
      assertThat(record.getType(), is(RC_GOOD_EMPTY));
      assertThat(record.isPersistent(), is(true));
      assertThat(record.getId(), is(id));
      assertThat(record.getVersion(), not(is(version)));
      assertThat(record.has("recordField"), is(true));
      assertThat(record.has("recordIdField"), is(true));
      assertThat(record.has("bigDecimalField"), is(true));
      assertThat(record.has("byteField"), is(true));
      assertThat(record.has("dateField"), is(true));
      assertThat(record.has("doubleField"), is(true));
      assertThat(record.has("floatField"), is(true));
      assertThat(record.has("integerField"), is(true));
      assertThat(record.has("longField"), is(true));
      assertThat(record.has("shortField"), is(true));
      assertThat(record.has("stringField"), is(true));
      assertThat(record.getRecord("recordField"), is(otherRecord));
      assertThat((BigDecimal) record.get("bigDecimalField"), is(bigDecimalValue));
      assertThat((Byte) record.get("byteField"), is(byteValue));
      assertThat((Date) record.get("dateField"), is(dateValue));
      assertThat((Double) record.get("doubleField"), is(doubleValue));
      assertThat((Float) record.get("floatField"), is(floatValue));
      assertThat((Integer) record.get("integerField"), is(integerValue));
      assertThat((Long) record.get("longField"), is(longValue));
      assertThat((Short) record.get("shortField"), is(shortValue));
      assertThat((String) record.get("stringField"), is("newStringValue"));
      assertThat(record.keySet().size(), is(11));

      // delete then get
      session.delete(record);
      assertThat(session.get(id), is(nullValue()));
    }
  }

  private static void assertGetIdThrowsIllegalStateException(Record record) {
    try {
      record.getId();
      fail("Expected IllegalStateException");
    }
    catch (IllegalStateException e) {
      // expected
    }
  }

  private static void assertGetVersionThrowsIllegalStateException(Record record) {
    try {
      record.getVersion();
      fail("Expected IllegalStateException");
    }
    catch (IllegalStateException e) {
      // expected
    }
  }

  private static void assertGetThrowsNullPointerException(Record record, String key) {
    try {
      record.get(key);
      fail("Expected NullPointerException");
    }
    catch (NullPointerException e) {
      // expected
    }
  }

  private static void assertGetRecordThrowsNullPointerException(Record record, String key) {
    try {
      record.getRecord(key);
      fail("Expected NullPointerException");
    }
    catch (NullPointerException e) {
      // expected
    }
  }
}