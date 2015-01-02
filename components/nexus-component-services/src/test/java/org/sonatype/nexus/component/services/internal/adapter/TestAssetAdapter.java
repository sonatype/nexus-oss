/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

/**
 * Entity adapter for test assets.
 */
public class TestAssetAdapter
    extends AssetAdapter
{
  public static final String CLASS_NAME = "testasset";

  public static final String P_DOWNLOAD_COUNT = "downloadCount";

  public TestAssetAdapter() {
    super(CLASS_NAME);
  }

  @Override
  public void initClass(final OClass oClass) {
    createRequiredAutoIndexedProperty(oClass, P_DOWNLOAD_COUNT, OType.LONG, false);
  }
}
