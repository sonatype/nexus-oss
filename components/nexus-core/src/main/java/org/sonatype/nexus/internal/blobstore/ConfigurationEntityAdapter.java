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
package org.sonatype.nexus.internal.blobstore;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.entity.EntityAdapter;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * since 3.0
 */
@Named
@Singleton
public class ConfigurationEntityAdapter
    extends EntityAdapter<BlobStoreConfigurationEntity>
{

  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("blobstore")
      .type(BlobStoreConfigurationEntity.class)
      .build();

  private static final String P_BLOBSTORE_NAME = "name";

  private static final String P_BLOBSTORE_PATH = "path";

  private static final String I_BLOBSTORE_NAME = "blobstore_name_idx";

  private static final String I_BLOBSTORE_PATH = "blobstore_path_idx";

  public ConfigurationEntityAdapter() {
    super(DB_CLASS);
  }

  @Override
  protected void defineType(final OClass type) {
    type.createProperty(P_BLOBSTORE_NAME, OType.STRING).setMandatory(true).setNotNull(true);
    type.createProperty(P_BLOBSTORE_PATH, OType.STRING).setMandatory(true).setNotNull(true);
    type.createIndex(I_BLOBSTORE_NAME, INDEX_TYPE.UNIQUE, P_BLOBSTORE_NAME);
    type.createIndex(I_BLOBSTORE_PATH, INDEX_TYPE.UNIQUE, P_BLOBSTORE_PATH);
  }

  @Override
  protected BlobStoreConfigurationEntity newEntity() {
    return new BlobStoreConfigurationEntity();
  }

  @Override
  protected void readFields(final ODocument document, final BlobStoreConfigurationEntity entity) {
    String blobStoreName = document.field(P_BLOBSTORE_NAME, OType.STRING);
    String path = document.field(P_BLOBSTORE_PATH, OType.STRING);
    entity.setName(blobStoreName);
    entity.setPath(path);
  }

  @Override
  protected void writeFields(final ODocument document, final BlobStoreConfigurationEntity entity) {
    document.field(P_BLOBSTORE_NAME, entity.getName());
    document.field(P_BLOBSTORE_PATH, entity.getPath());
  }
}
