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
import org.sonatype.nexus.componentmetadata.RecordId;
import org.sonatype.nexus.componentmetadata.RecordQuery;
import org.sonatype.nexus.componentmetadata.RecordStoreSchema;
import org.sonatype.nexus.componentmetadata.RecordStoreSession;
import org.sonatype.nexus.componentmetadata.RecordType;
import org.sonatype.nexus.orient.RecordIdObfuscator;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * OrientDB implementation of {@link RecordStoreSession}.
 *
 * @since 3.0
 */
class OrientRecordStoreSession
    implements RecordStoreSession
{
  private final ODatabaseDocumentTx db;

  private final RecordIdObfuscator recordIdObfuscator;

  private final OSchema oSchema;

  private final RecordStoreSchema schema;

  private boolean closed;

  public OrientRecordStoreSession(final ODatabaseDocumentTx db,
                                  final RecordIdObfuscator recordIdObfuscator) {
    this.db = db;
    this.recordIdObfuscator = recordIdObfuscator;
    this.oSchema = db.getMetadata().getSchema();
    this.schema = new OrientRecordStoreSchema(this, oSchema);
  }

  @Override
  public RecordStoreSchema getSchema() {
    return schema;
  }

  @Override
  public Record create(final RecordType type) {
    checkOpen();
    checkType(type);

    try {
      ODocument document = db.newInstance(type.getName());
      return new OrientRecord(this, type, document);
    }
    catch (OSchemaException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Record get(final RecordId recordId) {
    checkOpen();
    checkNotNull(recordId);
    OClass oClass = checkOrientClass(recordId.getTypeName());

    ODocument document = db.load(recordIdObfuscator.decode(oClass, recordId.getValue()));
    if (document == null) {
      return null;
    }
    return getRecord(document);
  }

  @Override
  public List<Record> find(final RecordQuery query)
  {
    checkOpen();
    checkNotNull(query);
    checkType(query.getType());

    String queryText = new OrientQueryBuilder(
        query,
        checkOrientClass(query.getType().getName()),
        recordIdObfuscator)
            .build();
    List<ODocument> documents = db.query(new OSQLSynchQuery<ODocument>(queryText), query.getFilter());
    List<Record> records = Lists.newArrayListWithCapacity(documents.size());
    for (ODocument document : documents) {
      records.add(new OrientRecord(this, query.getType(), document));
    }
    return records;
  }

  @Override
  public long count(final RecordQuery query) {
    checkOpen();
    checkNotNull(query);
    checkType(query.getType());

    if (query.getFilter().isEmpty()) {
      return db.countClass(query.getType().getName());
    }

    String queryText = new OrientQueryBuilder(
        query,
        checkOrientClass(query.getType().getName()),
        recordIdObfuscator)
            .withCount(true)
            .build();
    List<ODocument> results = db.query(new OSQLSynchQuery<>(queryText), query.getFilter());
    return results.get(0).field("COUNT");
  }

  @Override
  public void delete(final Record record) {
    checkOpen();

    db.delete(getRid(record.getId()));
  }

  @Override
  public void close() {
    db.close();
    closed = true;
  }

  protected void execute(String sql) {
    db.command(new OCommandSQL(sql)).execute();
  }

  protected void checkOpen() {
    checkState(!closed, "Session is closed");
  }

  protected ORID getRid(RecordId recordId) {
    return recordIdObfuscator.decode(checkOrientClass(recordId.getTypeName()), (recordId.getValue()));
  }

  protected RecordId getRecordId(OClass oClass, ORID rid) {
    return new RecordId(oClass.getName(), recordIdObfuscator.encode(oClass, rid));
  }

  protected Record getRecord(ODocument document) {
    RecordType type = schema.getType(document.getClassName());
    checkArgument(type != null, "Class is not registered: %s", document.getClassName());
    return new OrientRecord(this, type, document);
  }

  private OClass checkOrientClass(String name) {
    OClass oClass = oSchema.getClass(name);
    checkArgument(oClass != null, "No such Orient class: %s", name);
    return oClass;
  }

  private RecordType checkType(RecordType type) {
    checkNotNull(type);
    checkArgument(schema.hasType(type.getName()), "No such type in schema: %s", type.getName());
    checkArgument(schema.getType(type.getName()).equals(type),
        "A different definition for that type exists in the schema. Existing: %s, Given: %s",
        schema.getType(type.getName()), type);
    return type;
  }
}
