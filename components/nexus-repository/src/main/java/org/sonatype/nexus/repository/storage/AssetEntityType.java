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

package org.sonatype.nexus.repository.storage;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.entity.EntityType;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;

import static org.sonatype.nexus.repository.storage.StorageFacet.P_ATTRIBUTES;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BLOB_REF;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_BUCKET;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_COMPONENT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_CONTENT_TYPE;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_FORMAT;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_LAST_UPDATED;
import static org.sonatype.nexus.repository.storage.StorageFacet.P_SIZE;

/**
 * {@link Asset} entity-adapter.
 *
 * @since 3.0
 */
public class AssetEntityType
    extends EntityType
{
  public static final String DB_CLASS = new OClassNameBuilder()
      .type(Asset.class)
      .build();

  private static final String I_BUCKET = "asset_bucket_idx";

  private static final String I_COMPONENT = "asset_component_idx";

  private BucketEntityType bucketEntityType;

  private ComponentEntityType componentEntityType;

  public AssetEntityType(final BucketEntityType bucketEntityType,
                         final ComponentEntityType componentEntityType)
  {
    super(DB_CLASS);
    this.bucketEntityType = bucketEntityType;
    this.componentEntityType = componentEntityType;
  }

  @Override
  protected void defineType(final OClass type) {
    type.createProperty(P_BUCKET, OType.LINK, bucketEntityType.getType());
    type.createProperty(P_COMPONENT, OType.LINK, componentEntityType.getType());
    type.createProperty(P_FORMAT, OType.STRING);
    type.createProperty(P_LAST_UPDATED, OType.DATETIME);
    type.createProperty(P_SIZE, OType.INTEGER);
    type.createProperty(P_CONTENT_TYPE, OType.STRING);
    type.createProperty(P_BLOB_REF, OType.STRING);
    type.createProperty(P_ATTRIBUTES, OType.EMBEDDEDMAP);

    type.createIndex(I_BUCKET, INDEX_TYPE.NOTUNIQUE, P_BUCKET);
    type.createIndex(I_COMPONENT, INDEX_TYPE.NOTUNIQUE, P_COMPONENT);
  }

}
