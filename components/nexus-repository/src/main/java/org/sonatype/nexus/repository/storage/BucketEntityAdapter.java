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

import java.util.List;

import javax.annotation.Nullable;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.entity.EntityType;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * {@link Bucket} entity-adapter.
 *
 * @since 3.0
 */
public class BucketEntityAdapter
    extends EntityType
{
  public static final String DB_CLASS = new OClassNameBuilder()
      .type(Bucket.class)
      .build();

  public BucketEntityAdapter() {
    super(DB_CLASS);
  }

  @Override
  protected void defineType(final OClass type) {
    type.createProperty(P_REPOSITORY_NAME, OType.STRING);
    // TODO create indexes
  }
}
