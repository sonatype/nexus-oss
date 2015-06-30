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

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.entity.CollectionEntityAdapter;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static org.sonatype.nexus.repository.storage.StorageFacet.P_REPOSITORY_NAME;

/**
 * {@link Bucket} entity-adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class BucketEntityAdapter
    extends CollectionEntityAdapter<Bucket>
{
  public static final String DB_CLASS = new OClassNameBuilder()
      .type(Bucket.class)
      .build();

  private static final String I_REPOSITORY_NAME = DB_CLASS + ".repository_name_idx";

  @Inject
  public BucketEntityAdapter() {
    super(DB_CLASS);
  }

  @Override
  protected void defineType(final OClass type) {
    type.createProperty(P_REPOSITORY_NAME, OType.STRING).setMandatory(true).setNotNull(true);

    type.createIndex(I_REPOSITORY_NAME, INDEX_TYPE.UNIQUE, P_REPOSITORY_NAME);
  }

  @Override
  protected Bucket newEntity() {
    return new Bucket();
  }

  @Override
  protected void readFields(final ODocument document, final Bucket entity) {
    String repositoryName = document.field(P_REPOSITORY_NAME, OType.STRING);

    entity.repositoryName(repositoryName);
  }

  @Override
  protected void writeFields(final ODocument document, final Bucket entity) {
    document.field(P_REPOSITORY_NAME, entity.repositoryName());
  }

  @Nullable
  Bucket getByRepositoryName(final ODatabaseDocumentTx db, final String repositoryName) {
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
        "select from " + DB_CLASS + " where " + P_REPOSITORY_NAME + " = ?"
    );
    List<ODocument> results = db.command(query).execute(repositoryName);
    if (results.isEmpty()) {
      return null;
    }
    return readEntity(results.get(0));
  }

}
