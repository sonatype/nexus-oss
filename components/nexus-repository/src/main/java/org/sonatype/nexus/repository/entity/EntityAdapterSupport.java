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
package org.sonatype.nexus.repository.entity;

import javax.inject.Inject;

import org.sonatype.nexus.orient.RecordIdObfuscator;
import org.sonatype.sisu.goodies.common.ComponentSupport;

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
 * Support for {@link EntityAdapter} implementations.
 *
 * @since 3.0
 */
public abstract class EntityAdapterSupport<T extends EntitySupport>
  extends ComponentSupport
  implements EntityAdapter<T>
{
  private final String className;

  private RecordIdObfuscator recordIdObfuscator;

  private OClass type;

  public EntityAdapterSupport(final String className) {
    this.className = checkNotNull(className);
  }

  @Inject
  public void installDependencies(final RecordIdObfuscator recordIdObfuscator) {
    this.recordIdObfuscator = checkNotNull(recordIdObfuscator);
  }

  protected RecordIdObfuscator getRecordIdObfuscator() {
    checkState(recordIdObfuscator != null);
    return recordIdObfuscator;
  }

  public OClass getType() {
    checkState(type != null, "No registered");
    return type;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "className='" + className + '\'' +
        '}';
  }

  @Override
  public void register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    type = schema.getClass(className);
    if (type == null) {
      type = schema.createClass(className);
      defineSchema(type);

      log.debug("Created schema class '{}': properties={}, indexes={}",
          type,
          type.properties(),
          type.getIndexes()
      );
    }
  }

  protected abstract void defineSchema(final OClass type);

  //
  // BREAD operations
  //

  protected abstract T newEntity();

  protected abstract void readFields(final ODocument document, final T entity);

  protected abstract void writeFields(final ODocument document, final T entity);

  @Override
  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(className);
  }

  @Override
  public T read(final ODocument document) {
    checkNotNull(document);

    T entity = newEntity();
    readFields(document, entity);
    setMetadata(entity, document);

    return entity;
  }

  @Override
  public ODocument edit(final ODocument document, final T entity) {
    checkNotNull(document);
    checkNotNull(entity);

    writeFields(document, entity);
    setMetadata(entity, document);

    return document.save();
  }

  @Override
  public ODocument add(final ODatabaseDocumentTx db, final T entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(className);
    return edit(doc, entity);
  }

  // TODO: delete

  //
  // Metadata support
  //

  /**
   * Set metadata on entity.
   */
  public void setMetadata(final T entity, final ODocument doc) {
    checkNotNull(entity != null);
    checkNotNull(doc != null);
    EntityId id = new EntityId(getRecordIdObfuscator().encode(getType(), doc.getIdentity()));
    EntityVersion version = new EntityVersion(doc.getRecordVersion().toString());
    entity.setEntityMetadata(new EntityMetadata(id, version));
  }

  /**
   * Get metadata from entity.
   */
  public EntityMetadata getMetadata(final T entity) {
    checkNotNull(entity != null);
    EntityMetadata metadata = entity.getEntityMetadata();
    checkState(metadata != null);
    return metadata;
  }

  /**
   * Return record identity of entity.
   */
  public ORID recordIdentity(final T entity) {
    EntityMetadata metadata = getMetadata(entity);
    return getRecordIdObfuscator().decode(getType(), metadata.getId().getValue());
  }

  /**
   * Return record version of entity.
   */
  public ORecordVersion recordVersion(final T entity) {
    EntityMetadata metadata = getMetadata(entity);
    ORecordVersion version = new OSimpleVersion();
    version.getSerializer().fromString(metadata.getVersion().getValue(), version);
    return version;
  }
}
