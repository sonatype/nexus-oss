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

import org.sonatype.nexus.component.services.adapter.AssetAdapter;
import org.sonatype.nexus.component.services.model.TestAsset;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

/**
 * Entity adapter for {@link TestAsset}.
 */
public class TestAssetAdapter
    extends AssetAdapter<TestAsset>
{
  public static final String P_DOWNLOAD_COUNT = "downloadCount";

  @Override
  public Class<TestAsset> getEntityClass() {
    return TestAsset.class;
  }

  @Override
  public void initStorageClass(final OClass oClass) {
    createRequiredAutoIndexedProperty(oClass, P_DOWNLOAD_COUNT, OType.LONG, false);
  }

  @Override
  public void populateDocument(final TestAsset entity, final ODocument document) {
    setValueOrNull(document, P_DOWNLOAD_COUNT, entity.getDownloadCount());
  }

  @Override
  public void populateEntity(final ODocument document, final TestAsset entity) {
    entity.setDownloadCount((Long) document.field(P_DOWNLOAD_COUNT));
  }
}
