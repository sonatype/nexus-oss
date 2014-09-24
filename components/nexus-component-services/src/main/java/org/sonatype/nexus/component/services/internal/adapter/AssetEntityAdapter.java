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
package org.sonatype.nexus.component.services.internal.adapter;

import java.util.Date;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.ComponentId;
import org.sonatype.nexus.component.services.adapter.EntityAdapterSupport;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapter for converting between {@link Asset}s and OrientDB {@code ODocument}s with associated {@code Blob}s.
 *
 * @since 3.0
 */
public class AssetEntityAdapter
  extends EntityAdapterSupport
{
  public static final String ORIENT_CLASS_NAME = new OClassNameBuilder().type(Asset.class).build();

  /** OrientDB property name for the asset's globally unique id (componentId + ":" + path). */
  public static final String P_ID = "id";

  /** OrientDB property name for the component in which the asset belongs. */
  public static final String P_COMPONENT = "component";

  /** OrientDB property name for the unique id of the asset within the component. */
  public static final String P_PATH = "path";

  /** OrientDB property name for the date the asset was first created. */
  public static final String P_FIRST_CREATED = "firstCreated";

  /** OrientDB property name for the asset's optional mime type. */
  public static final String P_CONTENT_TYPE = "contentType";

  /** OrientDB property name for the known locations of the asset content; a map of blob ids keyed by blobstore id. */
  public static final String P_BLOB_REFS = "blobRefs";

  /**
   * Creates the OrientDB class and associated indexes in the database if needed.
   */
  public void registerStorageClass(ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    if (!schema.existsClass(ORIENT_CLASS_NAME)) {
      OClass oClass = schema.createClass(ORIENT_CLASS_NAME);
      createRequiredAutoIndexedProperty(oClass, P_ID, OType.STRING, true);
      createRequiredAutoIndexedProperty(oClass, P_COMPONENT, OType.LINK, false);
      createRequiredProperty(oClass, P_PATH, OType.STRING);
      createRequiredProperty(oClass, P_FIRST_CREATED, OType.DATETIME);
      createOptionalProperty(oClass, P_CONTENT_TYPE, OType.STRING);
      createRequiredProperty(oClass, P_BLOB_REFS, OType.EMBEDDEDMAP);
      logCreatedClassInfo(oClass);
    }
  }

  /**
   * Converts an {@code ODocument} and associated {@code Blob} to an {@link Asset} object.
   */
  public Asset convertToAsset(ODocument document, Blob blob) {
    final String assetId = document.field(P_ID);
    ComponentId componentId = new ComponentId() {
      @Override
      public String asUniqueString() {
        return assetId.split(":")[0];
      }
    };
    String path = document.field(P_PATH);
    String contentType = document.field(P_CONTENT_TYPE);
    Date firstCreated = document.field(P_FIRST_CREATED);
    return new BlobAsset(componentId, blob, path, contentType, new DateTime(firstCreated));
  }

  public static String assetId(ComponentId componentId, String assetPath) {
    return String.format("%s:%s", componentId.asUniqueString(), assetPath);
  }
}
