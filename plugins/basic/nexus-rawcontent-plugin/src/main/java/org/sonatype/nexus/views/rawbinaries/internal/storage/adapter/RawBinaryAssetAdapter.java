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
package org.sonatype.nexus.views.rawbinaries.internal.storage.adapter;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.services.adapter.AssetAdapter;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;

/**
 * Entity adapter for raw binary assets.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RawBinaryAssetAdapter
    extends AssetAdapter
{
  public static final String CLASS_NAME = "rawbinaryasset";

  public RawBinaryAssetAdapter() {
    super(CLASS_NAME);
  }

  @Override
  protected void initClass(final OClass storageClass) {
    // no custom properties, but add a unique index on the inherited path property
    String indexName = String.format("%s.%s", storageClass.getName(), P_PATH);
    storageClass.createIndex(indexName, INDEX_TYPE.UNIQUE, P_PATH);
  }
}
