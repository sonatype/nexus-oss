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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobStoreConfiguration;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.entity.FieldCopier;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Manage persistence of {@link BlobStoreConfiguration}.
 * since 3.0
 */
@Named
@Singleton
public class ConfigurationEntityAdapter
    extends ComponentSupport
{

  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("blobstore")
      .type("configuration")
      .build();

  private static final String P_NAME = "name";
  
  private static final String P_RECIPE_NAME = "recipe_name";

  private static final String P_ATTRIBUTES = "attributes";

  private static final String I_NAME = "blobstore_name_idx";

  /**
   * Register the required schema.
   * @param db
   * @return the resulting Schema class
   */
  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    
    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if(type == null) {
      type = schema.createClass(DB_CLASS);
      type.createProperty(P_NAME, OType.STRING).setMandatory(true).setNotNull(true);
      type.createProperty(P_RECIPE_NAME, OType.STRING).setMandatory(true).setNotNull(true);
      type.createProperty(P_ATTRIBUTES, OType.EMBEDDEDMAP).setMandatory(true).setNotNull(true);
      type.createIndex(I_NAME, INDEX_TYPE.UNIQUE, P_NAME);
      
      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new BlobStoreConfiguration.
   * @param db
   * @param entity
   * @return
   */
  public ODocument create(final ODatabaseDocumentTx db, final BlobStoreConfiguration entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);   
  }

  /**
   * Browse all documents.
   */
  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(DB_CLASS);
  }

  /**
   * Get all BlobStoreConfigurations.
   * 
   * @param db
   * @return collection of BlobStoreConfigurations
   */
  public Iterable<BlobStoreConfiguration> get(final ODatabaseDocumentTx db) {
    return Iterables.transform(browse(db), new Function<ODocument, BlobStoreConfiguration>()
    {
      @Nullable
      @Override
      public BlobStoreConfiguration apply(final ODocument input) {
        return input == null ? null : read(input);
      }
    });
  }

  /**
   * Get a BlobStoreConfiguration by name.
   * 
   * @param db
   * @param name
   * @return the configuration or null if not found
   */
  @Nullable
  public ODocument get(final ODatabaseDocumentTx db, final String name) {
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
        "SELECT FROM " + DB_CLASS + " WHERE " + P_NAME + " = ?"
    );
    List<ODocument> results = db.command(query).execute(name);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);  
  }

  /**
   * Deletes a BlobStoreConfiguration.
   *
   * @return true if configuration was deleted
   */
  public boolean delete(final ODatabaseDocumentTx db, final String name) {
    OCommandSQL command = new OCommandSQL(
        "DELETE FROM " + DB_CLASS + " WHERE " + P_NAME + " = ?"
    );
    int records = db.command(command).execute(name);
    return records == 1;
  }
  
  private BlobStoreConfiguration read(final ODocument document) {
    final BlobStoreConfiguration entity = new BlobStoreConfiguration();
    String blobStoreName = document.field(P_NAME, OType.STRING);
    String recipeName = document.field(P_RECIPE_NAME, OType.STRING);
    Map<String, Map<String, Object>> attributes = document.field(P_ATTRIBUTES, OType.EMBEDDEDMAP);
    
    // deeply copy attributes to divorce from document
    if (attributes != null) {
      //noinspection unchecked
      attributes = FieldCopier.copy(attributes);
    }
    
    entity.setName(blobStoreName);
    entity.setRecipeName(recipeName);
    entity.setAttributes(attributes);
    return entity;
  }

  private ODocument write(final ODocument document, final BlobStoreConfiguration entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_NAME, entity.getName());
    document.field(P_RECIPE_NAME, entity.getRecipeName());
    document.field(P_ATTRIBUTES, entity.getAttributes());
    return document.save();
  }
}
