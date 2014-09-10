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

import java.util.Set;

import org.sonatype.nexus.componentmetadata.Record;
import org.sonatype.nexus.componentmetadata.RecordId;
import org.sonatype.nexus.componentmetadata.RecordType;
import org.sonatype.nexus.componentmetadata.RecordVersion;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * OrientDB implementation of {@link Record}.
 *
 * @since 3.0
 */
class OrientRecord
    implements Record
{
  private final OrientRecordStoreSession session;

  private final RecordType type;

  protected ODocument document;

  public OrientRecord(final OrientRecordStoreSession session,
                      final RecordType type,
                      final ODocument document) {
    this.session = session;
    this.type = type;
    this.document = document;
  }

  @Override
  public RecordType getType() {
    session.checkOpen();

    return type;
  }

  @Override
  public boolean isPersistent() {
    session.checkOpen();

    return document.getIdentity().isPersistent();
  }

  @Override
  public RecordId getId() {
    session.checkOpen();
    checkPersistent();

    return session.getRecordId(document.getSchemaClass(), document.getIdentity());
  }

  @Override
  public RecordVersion getVersion() {
    session.checkOpen();
    checkPersistent();

    return new RecordVersion(Integer.valueOf(document.getVersion()).toString());
  }

  @Override
  public boolean has(String key) {
    session.checkOpen();

    return document.field(key) != null;
  }

  @Override
  public <T> T get(final String key) {
    session.checkOpen();

    return checkValueNotNull(key);
  }

  @Override
  public Record getRecord(String key) {
    session.checkOpen();

    Object value = checkValueNotNull(key);
    if (value instanceof ODocument) {
      return session.getRecord((ODocument) value);
    }
    throw new ClassCastException(String.format("Cannot convert to Record: %s", value.getClass()));
  }

  @Override
  public Record set(final String key, final Object value) {
    session.checkOpen();
    checkNotNull(value);

    if (value instanceof OrientRecord) {
      document.field(key, ((OrientRecord) value).document);
    }
    else if (value instanceof RecordId) {
      document.field(key, session.getRid((RecordId) value));
    }
    else {
      document.field(key, value);
    }
    return this;
  }

  @Override
  public void remove(final String key) {
    session.checkOpen();

    document.removeField(key);
  }

  @Override
  public Set<String> keySet() {
    session.checkOpen();

    return Sets.newHashSet(document.fieldNames());
  }

  @Override
  public Record save() {
    session.checkOpen();
    document = document.save();
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (!(obj instanceof OrientRecord)) return false;
    final OrientRecord other = (OrientRecord) obj;
    return Objects.equal(getType(), other.getType())
        && Objects.equal(getId(), other.getId())
        && Objects.equal(getVersion(), other.getVersion())
        && Objects.equal(document, other.document);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getType(), getId(), getVersion(), document);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(OrientRecord.class)
        .add("type", getType())
        .add("id", getId())
        .add("version", getVersion())
        .add("document", document)
        .toString();
  }

  private <T> T checkValueNotNull(String key) {
    T value = document.field(key);
    return checkNotNull(value, String.format("Undefined value for field: %s", key));
  }

  private void checkPersistent() {
    checkState(isPersistent(), "Record has not been saved yet");
  }
}
