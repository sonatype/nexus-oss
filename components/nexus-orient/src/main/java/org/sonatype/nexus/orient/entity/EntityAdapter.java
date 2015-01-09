/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.version.ORecordVersion;
import com.orientechnologies.orient.core.version.OSimpleVersion;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Entity adapter.
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
    type = schema.getClass(typeName);
    if (type == null) {
      type = schema.createClass(typeName);
      defineType(type);

      log.info("Created schema type '{}': properties={}, indexes={}",
          type,
          type.properties(),
          type.getIndexes()
      );
    }
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

  protected abstract void readFields(final ODocument document, final T entity);

  protected abstract void writeFields(final ODocument document, final T entity);

  // TODO: Clean up api and return types

  /**
   * Browse all documents.
   */
  @VisibleForTesting
  Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(typeName);
  }

  /**
   * Browse all entities, appending to given collection.
   */
  public List<T> browse(final ODatabaseDocumentTx db, final List<T> entities) {
    checkNotNull(entities);
    for (ODocument document : browse(db)) {
      entities.add(read(document));
    }
    return entities;
  }

  /**
   * Read entity from document.
   */
  @VisibleForTesting
  T read(final ODocument document) {
    checkNotNull(document);

    T entity = newEntity();
    readFields(document, entity);
    setMetadata(entity, document);

    return entity;
  }

  /**
   * Edit entity.
   */
  public ODocument edit(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ORID rid = recordIdentity(entity);
    ODocument document = db.getRecord(rid);
    checkState(document != null);

    return edit(document, entity);
  }

  /**
   * Edit document from entity.
   */
  @VisibleForTesting
  ODocument edit(final ODocument document, final T entity) {
    checkNotNull(document);
    checkNotNull(entity);

    // TODO: MVCC

    writeFields(document, entity);
    setMetadata(entity, document);

    return document.save();
  }

  /**
   * Add new entity.
   */
  public ODocument add(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    // new entity must not already have metadata
    checkState(entity.getEntityMetadata() == null);

    ODocument doc = db.newInstance(typeName);
    return edit(doc, entity);
  }

  /**
   * Delete an entity.
   */
  public void delete(final ODatabaseDocumentTx db, final T entity) {
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
  private static class AttachedEntityMetadata
      implements EntityMetadata
  {
    private final EntityAdapter owner;

    private final ORID rid;

    private final ORecordVersion rversion;

    private EntityId id;

    private EntityVersion version;

    public AttachedEntityMetadata(final EntityAdapter owner, final ODocument document) {
      this.owner = checkNotNull(owner);
      checkNotNull(document);
      this.rid = document.getIdentity();
      this.rversion = document.getRecordVersion();
    }

    @Override
    public EntityId getId() {
      if (id == null) {
        id = new EntityId(owner.getRecordIdObfuscator().encode(owner.getType(), rid));
      }
      return id;
    }

    @Override
    public EntityVersion getVersion() {
      if (version == null) {
        version = new EntityVersion(rversion.toString());
      }
      return version;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "rid=" + rid +
          ", rversion=" + rversion +
          ", id=" + id +
          ", version=" + version +
          '}';
    }
  }

  /**
   * Set metadata on entity.
   */
  private void setMetadata(final T entity, final ODocument doc) {
    checkNotNull(entity != null);
    assert entity != null;
    entity.setEntityMetadata(new AttachedEntityMetadata(this, doc));
  }

  /**
   * Get metadata from entity.
   */
  private EntityMetadata getMetadata(final T entity) {
    checkNotNull(entity != null);
    assert entity != null;
    EntityMetadata metadata = entity.getEntityMetadata();
    checkState(metadata != null);
    return metadata;
  }

  /**
   * Return record identity of entity.
   */
  private ORID recordIdentity(final T entity) {
    EntityMetadata metadata = getMetadata(entity);
    if (metadata instanceof AttachedEntityMetadata) {
      return ((AttachedEntityMetadata) metadata).rid;
    }
    return getRecordIdObfuscator().decode(getType(), metadata.getId().getValue());
  }

  /**
   * Return record version of entity.
   */
  private ORecordVersion recordVersion(final T entity) {
    EntityMetadata metadata = getMetadata(entity);
    if (metadata instanceof AttachedEntityMetadata) {
      return ((AttachedEntityMetadata) metadata).rversion;
    }
    ORecordVersion version = new OSimpleVersion();
    version.getSerializer().fromString(metadata.getVersion().getValue(), version);
    return version;
  }

  //
  // Object copy helpers
  //

  private Object maybeCopy(final Object source) {
    if (source instanceof Map) {
      return copy((Map) source);
    }
    else if (source instanceof List) {
      return copy((List) source);
    }
    else if (source instanceof Set) {
      return copy((Set)source);
    }
    return source;
  }

  @SuppressWarnings("unchecked")
  protected Map copy(final Map<?, ?> source) {
    Map target = Maps.newHashMapWithExpectedSize(source.size());
    for (Map.Entry<?, ?> entry : source.entrySet()) {
      Object value = maybeCopy(entry.getValue());
      target.put(entry.getKey(), value);
    }
    return target;
  }

  @SuppressWarnings("unchecked")
  protected List copy(final List<?> source) {
    List target = Lists.newArrayListWithExpectedSize(source.size());
    for (Object value : source) {
      value = maybeCopy(value);
      target.add(value);
    }
    return target;
  }

  @SuppressWarnings("unchecked")
  protected Set copy(final Set<?> source) {
    Set target = Sets.newHashSetWithExpectedSize(source.size());
    for (Object value : source) {
      value = maybeCopy(value);
      target.add(value);
    }
    return target;
  }
}
