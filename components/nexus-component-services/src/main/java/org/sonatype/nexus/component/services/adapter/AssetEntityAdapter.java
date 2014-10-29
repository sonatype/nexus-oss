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
package org.sonatype.nexus.component.services.adapter;

import org.sonatype.nexus.component.model.Asset;
import org.sonatype.nexus.component.model.BaseAsset;
import org.sonatype.nexus.orient.OClassNameBuilder;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapter for converting between {@link Asset}s and OrientDB {@code ODocument}s.
 *
 * @since 3.0
 */
public class AssetEntityAdapter
  extends EntityAdapter<Asset>
{
  public static final String ORIENT_CLASS_NAME = new OClassNameBuilder().type(Asset.class).build();

  /** OrientDB property name for the component in which the asset belongs. */
  public static final String P_COMPONENT = "component";

  /** OrientDB property name for the date the asset was first created. */
  public static final String P_FIRST_CREATED = "firstCreated";

  /** OrientDB property name for the asset's optional mime type. */
  public static final String P_CONTENT_TYPE = "contentType";

  /** OrientDB property name for the known locations of the asset content; a map of blob ids keyed by blobstore id. */
  public static final String P_BLOB_REFS = "blobRefs";

  @Override
  public Class<Asset> getEntityClass() {
    return Asset.class;
  }

  /**
   * Creates the base OrientDB class and associated indexes in the database if needed.
   */
  public static void registerBaseClass(OSchema schema, Logger log) {
    checkNotNull(schema);
    if (!schema.existsClass(ORIENT_CLASS_NAME)) {
      OClass oClass = schema.createClass(ORIENT_CLASS_NAME);
      createRequiredAutoIndexedProperty(oClass, P_ID, OType.STRING, true);
      createRequiredAutoIndexedProperty(oClass, P_COMPONENT, OType.LINK, false);
      createRequiredProperty(oClass, P_FIRST_CREATED, OType.DATETIME);
      createOptionalProperty(oClass, P_CONTENT_TYPE, OType.STRING);
      createRequiredProperty(oClass, P_BLOB_REFS, OType.EMBEDDEDMAP);
      logCreatedClassInfo(log, oClass);
    }
  }

  @Override
  public final void registerStorageClass(ODatabaseDocumentTx db) {
    OSchema schema = checkNotNull(db).getMetadata().getSchema();
    registerBaseClass(schema, log);
  }

  @Override
  public void convertToDocument(final Asset entity, final ODocument document) {
    convertBasePropertiesToDocument(entity, document);
  }

  @Override
  public Asset convertToEntity(final ODocument document) {
    BaseAsset asset = new BaseAsset();
    convertBasePropertiesToEntity(document, asset);
    return asset;
  }

  /**
   * Adds base properties from the given {@code Asset} to the given {@code ODocument}.
   */
  public static void convertBasePropertiesToDocument(Asset entity, ODocument document) {
    setValueOrNull(document, P_ID, entity.getId());
    setValueOrNull(document, P_FIRST_CREATED, entity.getFirstCreated());
    setValueOrNull(document, P_CONTENT_TYPE, entity.getContentType());
  }

  /**
   * Adds base properties from the given {@code ODocument} to the given {@code Asset}.
   */
  public static void convertBasePropertiesToEntity(ODocument document, Asset asset) {
    asset.setId(getEntityIdOrNull(document, P_ID));
    asset.setFirstCreated(getDateTimeOrNull(document, P_FIRST_CREATED));
    asset.setContentType((String) document.field(P_CONTENT_TYPE));
  }
}
