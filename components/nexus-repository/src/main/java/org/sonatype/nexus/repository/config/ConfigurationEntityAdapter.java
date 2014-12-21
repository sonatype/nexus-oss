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
package org.sonatype.nexus.repository.config;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.repository.entity.EntityAdapter;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link Configuration} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ConfigurationEntityAdapter
  extends ComponentSupport
  implements EntityAdapter
{
  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("repository")
      .type(Configuration.class)
      .build();

  private static final String P_REPOSITORY_NAME = "repository_name";

  private static final String P_RECIPE_NAME = "recipe_name";

  private static final String P_ATTRIBUTES = "attributes";

  private static final String I_REPOSITORY_NAME = "repository_name_idx";

  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);

      type.createProperty(P_REPOSITORY_NAME, OType.STRING).setNotNull(true);
      type.createProperty(P_RECIPE_NAME, OType.STRING).setNotNull(true);
      type.createProperty(P_ATTRIBUTES, OType.EMBEDDEDMAP);

      type.createIndex(I_REPOSITORY_NAME, INDEX_TYPE.UNIQUE, P_REPOSITORY_NAME);

      log.info("Created schema class '{}': properties={}, indexes={}",
          type, type.properties(), type.getIndexes());
    }

    return type;
  }

  //
  // BREAD operations
  //

  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    return db.browseClass(DB_CLASS);
  }

  public Configuration read(final ODocument document) {
    checkNotNull(document);

    Configuration entity = new Configuration();

    String recipeName = document.field(P_RECIPE_NAME, OType.STRING);
    String repositoryName = document.field(P_REPOSITORY_NAME, OType.STRING);
    Map<String,Map<String,Object>> attributes = document.field(P_ATTRIBUTES, OType.EMBEDDEDMAP);

    entity.setRecipeName(recipeName);
    entity.setRepositoryName(repositoryName);
    entity.setAttributes(attributes);

    return entity;
  }

  public ODocument edit(final ODocument document, final Configuration entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_RECIPE_NAME, entity.getRecipeName());
    document.field(P_REPOSITORY_NAME, entity.getRepositoryName());
    document.field(P_ATTRIBUTES, entity.getAttributes());

    return document.save();
  }

  public ODocument add(final ODatabaseDocumentTx db, final Configuration entity) {
    checkNotNull(db);

    ODocument doc = db.newInstance(DB_CLASS);

    return edit(doc, entity);
  }

  // TODO: delete()
}
