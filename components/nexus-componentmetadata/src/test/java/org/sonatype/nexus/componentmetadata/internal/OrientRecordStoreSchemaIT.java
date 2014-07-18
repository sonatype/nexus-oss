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

import org.sonatype.nexus.componentmetadata.RecordStoreSchema;
import org.sonatype.nexus.componentmetadata.RecordStoreSession;

import com.google.common.collect.Sets;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OrientRecordStoreSchemaIT
    extends OrientRecordStoreITSupport
{
  // addType

  @Test(expected = IllegalStateException.class)
  public void addTypeBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    RecordStoreSchema schema = session.getSchema();
    session.close();
    schema.addType(RC_GOOD_EMPTY);
  }

  @Test(expected = NullPointerException.class)
  public void addTypeBadClassNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(null);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void addTypeBadFieldType() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_BAD_FIELDTYPE);
    }
  }

  // addType+hasType+getType+dropType+iterate

  @Test
  public void addTypeWithAllFieldTypesHasClassGetClassDropClassIterate() {
    try (RecordStoreSession session = store.openSession()) {
      RecordStoreSchema schema = session.getSchema();
      int origCount = Sets.newHashSet(schema).size();
      assertThat(Sets.newHashSet(schema).contains(RC_GOOD_ALLFIELDTYPES), is(false));

      assertThat(schema.addType(RC_GOOD_ALLFIELDTYPES), is(true));
      assertThat(schema.hasType(RC_GOOD_ALLFIELDTYPES.getName()), is(true));
      assertThat(schema.getType(RC_GOOD_ALLFIELDTYPES.getName()), is(RC_GOOD_ALLFIELDTYPES));
      assertThat(Sets.newHashSet(schema).contains(RC_GOOD_ALLFIELDTYPES), is(true));
      assertThat(Sets.newHashSet(schema).size(), is(origCount + 1));

      schema.dropType(RC_GOOD_ALLFIELDTYPES.getName());
      assertThat(schema.hasType(RC_GOOD_ALLFIELDTYPES.getName()), is(false));
      assertThat(Sets.newHashSet(schema).contains(RC_GOOD_ALLFIELDTYPES), is(false));
      assertThat(Sets.newHashSet(schema).size(), is(origCount));
    }
  }

  @Test
  public void addSimpleClassTwice() {
    try (RecordStoreSession session = store.openSession()) {
      RecordStoreSchema schema = session.getSchema();
      int origCount = Sets.newHashSet(schema).size();
      assertThat(schema.addType(RC_GOOD_EMPTY), is(true));
      assertThat(Sets.newHashSet(schema).size(), is(origCount + 1));
      assertThat(schema.addType(RC_GOOD_EMPTY), is(false));
      assertThat(Sets.newHashSet(schema).size(), is(origCount + 1));
    }
  }

  @Test
  public void addSubclassGetSubclassDropSubclass() {
    try (RecordStoreSession session = store.openSession()) {
      RecordStoreSchema schema = session.getSchema();
      int origCount = Sets.newHashSet(schema).size();
      assertThat(schema.addType(RC_GOOD_SUPERTYPE), is(true));
      assertThat(schema.addType(RC_GOOD_SUBTYPE), is(true));
      assertThat(Sets.newHashSet(schema).size(), is(origCount + 2));

      assertThat(schema.getType(RC_GOOD_SUBTYPE.getName()), is(RC_GOOD_SUBTYPE));

      schema.dropType(RC_GOOD_SUBTYPE.getName());
      assertThat(Sets.newHashSet(schema).size(), is(origCount + 1));
    }
  }

  @Test(expected = IllegalStateException.class)
  public void addSubclassBadMissingSuperclass() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().addType(RC_GOOD_SUBTYPE);
    }
  }

  // getType

  @Test(expected = IllegalStateException.class)
  public void getTypeBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    RecordStoreSchema schema = session.getSchema();
    session.close();
    schema.getType(RC_GOOD_EMPTY.getName());
  }

  @Test(expected = NullPointerException.class)
  public void getTypeBadNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().getType(null);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void getTypeBadMissing() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().getType(RC_GOOD_EMPTY.getName());
    }
  }

  // hasType

  @Test(expected = IllegalStateException.class)
  public void hasTypeBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    RecordStoreSchema schema = session.getSchema();
    session.close();
    schema.hasType(RC_GOOD_EMPTY.getName());
  }

  @Test(expected = NullPointerException.class)
  public void hasTypeBadNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().hasType(null);
    }
  }

  @Test
  public void hasTypeFalse() {
    try (RecordStoreSession session = store.openSession()) {
      assertThat(session.getSchema().hasType(RC_GOOD_EMPTY.getName()), is(false));
    }
  }

  // dropType

  @Test(expected = IllegalStateException.class)
  public void dropTypeBadSessionClosed() {
    RecordStoreSession session = store.openSession();
    RecordStoreSchema schema = session.getSchema();
    session.close();
    schema.hasType(RC_GOOD_EMPTY.getName());
  }

  @Test(expected = NullPointerException.class)
  public void dropTypeBadNull() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().dropType(null);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void dropTypeBadMissing() {
    try (RecordStoreSession session = store.openSession()) {
      session.getSchema().dropType(RC_GOOD_EMPTY.getName());
    }
  }

  @Test(expected = IllegalStateException.class)
  public void dropSuperclassBadSubclassExists() {
    try (RecordStoreSession session = store.openSession()) {
      RecordStoreSchema schema = session.getSchema();
      schema.addType(RC_GOOD_SUPERTYPE);
      schema.addType(RC_GOOD_SUBTYPE);
      schema.dropType(RC_GOOD_SUPERTYPE.getName());
    }
  }
}