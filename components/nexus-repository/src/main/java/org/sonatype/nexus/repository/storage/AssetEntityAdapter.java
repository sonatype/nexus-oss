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
package org.sonatype.nexus.repository.storage;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobRef;
import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.OIndexNameBuilder;

import com.google.common.collect.ImmutableMap;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BLOB_REF;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BUCKET;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CONTENT_TYPE;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_NAME;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_SIZE;

/**
 * {@link Asset} entity-adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class AssetEntityAdapter
    extends MetadataNodeEntityAdapter<Asset>
{
  public static final String DB_CLASS = new OClassNameBuilder()
      .type(Asset.class)
      .build();

  private static final String I_BUCKET_COMPONENT_NAME = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_BUCKET)
      .property(P_COMPONENT)
      .property(P_NAME)
      .build();

  private static final String I_COMPONENT = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_COMPONENT)
      .build();

  private final ComponentEntityAdapter componentEntityAdapter;

  @Inject
  public AssetEntityAdapter(final BucketEntityAdapter bucketEntityAdapter,
                            final ComponentEntityAdapter componentEntityAdapter)
  {
    super(DB_CLASS, bucketEntityAdapter);
    this.componentEntityAdapter = componentEntityAdapter;
  }

  @Override
  protected void defineType(final ODatabaseDocumentTx db, final OClass type) {
    super.defineType(type);
    type.createProperty(P_COMPONENT, OType.LINK, componentEntityAdapter.getType());
    type.createProperty(P_NAME, OType.STRING).setMandatory(true).setNotNull(true);
    type.createProperty(P_SIZE, OType.LONG);
    type.createProperty(P_CONTENT_TYPE, OType.STRING);
    type.createProperty(P_BLOB_REF, OType.STRING);

    ODocument metadata = db.newInstance()
        .field("ignoreNullValues", false)
        .field("mergeKeys", false);
    type.createIndex(I_BUCKET_COMPONENT_NAME, INDEX_TYPE.UNIQUE.name(), null, metadata,
        new String[]{P_BUCKET, P_COMPONENT, P_NAME}
    );
    type.createIndex(I_COMPONENT, INDEX_TYPE.NOTUNIQUE, P_COMPONENT);
  }

  @Override
  protected Asset newEntity() {
    return new Asset();
  }

  @Override
  protected void readFields(final ODocument document, final Asset entity) {
    super.readFields(document, entity);

    ORID componentId = document.field(P_COMPONENT, ORID.class);
    String name = document.field(P_NAME, OType.STRING);
    Long size = document.field(P_SIZE, OType.LONG);
    String contentType = document.field(P_CONTENT_TYPE, OType.STRING);
    String blobRef = document.field(P_BLOB_REF, OType.STRING);

    if (componentId != null) {
      entity.componentId(componentEntityAdapter.encode(componentId));
    }
    entity.name(name);
    entity.size(size);
    entity.contentType(contentType);
    if (blobRef != null) {
      entity.blobRef(BlobRef.parse(blobRef));
    }
  }

  @Override
  protected void writeFields(final ODocument document, final Asset entity) {
    super.writeFields(document, entity);

    EntityId componentId = entity.componentId();
    document.field(P_COMPONENT, componentId != null ? componentEntityAdapter.recordIdentity(componentId) : null);
    document.field(P_NAME, entity.name());
    document.field(P_SIZE, entity.size());
    document.field(P_CONTENT_TYPE, entity.contentType());
    BlobRef blobRef = entity.blobRef();
    document.field(P_BLOB_REF, blobRef != null ? blobRef.toString() : null);
  }

  Iterable<Asset> browseByComponent(final ODatabaseDocumentTx db, final Component component) {
    checkNotNull(component, "component");
    checkState(component.isPersisted(), "component must be persisted");

    Map<String, Object> parameters = ImmutableMap.<String, Object>of(
        "component", componentEntityAdapter.recordIdentity(component)
    );
    String query = String.format("select from %s where component = :component", DB_CLASS);
    Iterable<ODocument> docs = db.command(new OCommandSQL(query)).execute(parameters);
    return readEntities(docs);
  }
}
