/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.orient.entity;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.sonatype.nexus.common.entity.Entity;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.common.entity.EntityMetadata;
import org.sonatype.nexus.common.entity.EntityVersion;
import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.version.ORecordVersion;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.common.entity.EntityHelper.id;
import static org.sonatype.nexus.common.entity.EntityHelper.metadata;

/**
 * Support for entity-adapter implementations.
 *
 * @since 3.0
 */
public abstract class EntityAdapter<T extends Entity>
    extends ComponentSupport
{
  private final String typeName;

  private RecordIdObfuscator recordIdObfuscator;

  private OClass type;

  public EntityAdapter(final String typeName) {
    this.typeName = checkNotNull(typeName);
  }

  protected String getTypeName() {
    return typeName;
  }

  @Inject
  public void installDependencies(final RecordIdObfuscator recordIdObfuscator) {
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  protected RecordIdObfuscator getRecordIdObfuscator() {
    checkState(recordIdObfuscator != null);
    return recordIdObfuscator;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "typeName='" + typeName + '\'' +
        '}';
  }

  //
  // Schema
  //

  public void register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(typeName);
    if (type == null) {
      type = schema.createClass(typeName);
      defineType(db, type);

      log.info("Created schema type '{}': properties={}, indexes={}",
          type,
          type.properties(),
          type.getIndexes()
      );
    }
    this.type = type;
  }

  protected void defineType(final ODatabaseDocumentTx db, final OClass type) {
    defineType(type);
  }

  protected abstract void defineType(final OClass type);

  public OClass getType() {
    checkState(type != null, "Not registered");
    return type;
  }

  //
  // BREAD operations
  //

  protected abstract T newEntity();

  protected abstract void readFields(final ODocument document, final T entity) throws Exception;

  protected abstract void writeFields(final ODocument document, final T entity) throws Exception;

  /**
   * Browse all documents.
   */
  protected Iterable<ODocument> browseDocuments(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(typeName);
  }

  /**
   * Read entity from document.
   */
  protected T readEntity(final ODocument document) {
    checkNotNull(document);

    T entity = newEntity();
    try {
      readFields(document, entity);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
    setMetadata(entity, document);

    return entity;
  }

  /**
   * Write document from entity.
   */
  protected ODocument writeEntity(final ODocument document, final T entity) {
    checkNotNull(document);
    checkNotNull(entity);

    // TODO: MVCC

    try {
      writeFields(document, entity);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
    setMetadata(entity, document);

    return document.save();
  }

  /**
   * Edit entity.
   */
  protected ODocument editEntity(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ORID rid = recordIdentity(entity);
    ODocument document = db.getRecord(rid);
    checkState(document != null);

    return writeEntity(document, entity);
  }

  /**
   * Add new entity.
   */
  protected ODocument addEntity(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    // new entity must not already have metadata
    checkState(!entity.isPersisted());

    ODocument doc = db.newInstance(typeName);
    return writeEntity(doc, entity);
  }

  /**
   * Delete an entity.
   */
  protected void deleteEntity(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    // TODO: MVCC

    ORID rid = recordIdentity(entity);
    db.delete(rid);

    entity.setEntityMetadata(null);
  }

  //
  // Metadata support
  //

  /**
   * Attached {@link EntityMetadata} captures native details to simplify resolution w/o encoding.
   */
  protected static class AttachedEntityMetadata
      implements EntityMetadata
  {
    private final EntityAdapter owner;

    private ODocument document;

    private EntityId id;

    private EntityVersion version;

    public AttachedEntityMetadata(final EntityAdapter owner, final ODocument document) {
      this.owner = checkNotNull(owner);
      this.document = checkNotNull(document);
      checkNotNull(document);
    }

    @Override
    public EntityId getId() {
      if (id == null) {
        id = new AttachedEntityId(document, owner);
      }
      return id;
    }

    protected ORecordVersion getRecordVersion() {
      return document.getRecordVersion();
    }

    @Override
    public EntityVersion getVersion() {
      if (version == null) {
        version = new EntityVersion(getRecordVersion().toString());
      }
      return version;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "id=" + id +
          ", version=" + version +
          '}';
    }
  }

  /**
   * An {@link EntityId} that remains connected to the underlying ODocument. This is necessary with OrientDb
   * transactions mode, as ODocument ids change when the transaction is committed.
   */
  protected static class AttachedEntityId
      extends EntityId
  {
    private final EntityAdapter owner;

    private ODocument document;

    private String cachedValue;

    public AttachedEntityId(final ODocument document, final EntityAdapter owner) {
      super("");
      this.owner = checkNotNull(owner);
      this.document = checkNotNull(document);
    }

    @Nonnull
    @Override
    public String getValue() {
      if (cachedValue == null) {
        final ORID identity = document.getIdentity();
        checkState(!identity.isTemporary(), "attempted use of temporary/uncommitted document id");
        cachedValue = owner.getRecordIdObfuscator().encode(owner.getType(), identity);
      }
      return cachedValue;
    }

    protected ORID recordIdentity() {
      return document.getIdentity();
    }
  }

  /**
   * Set metadata on entity.
   */
  protected void setMetadata(final T entity, final ODocument doc) {
    checkNotNull(entity);
    entity.setEntityMetadata(new AttachedEntityMetadata(this, doc));
  }

  /**
   * Get metadata from entity.
   */
  protected EntityMetadata getMetadata(final T entity) {
    return metadata(entity);
  }

  public EntityId encode(final ORID id) {
    return new EntityId(getRecordIdObfuscator().encode(getType(), id));
  }

  /**
   * Return record identity of entity.
   */
  public ORID recordIdentity(final T entity) {
    return recordIdentity(id(entity));
  }

  public ORID recordIdentity(final EntityId id) {
    if (checkNotNull(id) instanceof AttachedEntityId) {
      return ((AttachedEntityId) id).recordIdentity();
    }
    return getRecordIdObfuscator().decode(getType(), id.toString());
  }
}
