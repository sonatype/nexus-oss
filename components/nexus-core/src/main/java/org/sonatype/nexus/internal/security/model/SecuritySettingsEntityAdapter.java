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
package org.sonatype.nexus.internal.security.model;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.security.settings.PasswordHelper;
import org.sonatype.nexus.security.settings.SecuritySettings;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link SecuritySettings} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class SecuritySettingsEntityAdapter
    extends ComponentSupport
{
  public static final String DB_CLASS = new OClassNameBuilder()
      .prefix("security")
      .type("settings")
      .build();

  public static final String P_ANONYMOUS_ACCESS_ENABLED = "anonymousAccessEnabled";

  public static final String P_ANONYMOUS_USERNAME = "anonymousUsername";

  public static final String P_ANONYMOUS_PASSWORD = "anonymousPassword";

  public static final String P_REALMS = "realms";

  private final PasswordHelper passwordHelper;

  @Inject
  public SecuritySettingsEntityAdapter(final PasswordHelper passwordHelper) {
    this.passwordHelper = checkNotNull(passwordHelper);
  }

  /**
   * Register schema.
   */
  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);

    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      type = schema.createClass(DB_CLASS);

      type.createProperty(P_ANONYMOUS_ACCESS_ENABLED, OType.BOOLEAN).setNotNull(true);
      type.createProperty(P_ANONYMOUS_USERNAME, OType.STRING).setNotNull(true);
      type.createProperty(P_ANONYMOUS_PASSWORD, OType.STRING).setNotNull(true);
      type.createProperty(P_REALMS, OType.EMBEDDEDLIST);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  /**
   * Create a new document and write entity.
   */
  public ODocument create(final ODatabaseDocumentTx db, final SecuritySettings entity) {
    checkNotNull(db);
    checkNotNull(entity);

    ODocument doc = db.newInstance(DB_CLASS);
    return write(doc, entity);
  }

  /**
   * Write entity to document.
   */
  public ODocument write(final ODocument document, final SecuritySettings entity) {
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_ANONYMOUS_ACCESS_ENABLED, entity.isAnonymousAccessEnabled());
    document.field(P_ANONYMOUS_USERNAME, entity.getAnonymousUsername());
    try {
      document.field(P_ANONYMOUS_PASSWORD, passwordHelper.encrypt(entity.getAnonymousPassword()));
    }
    catch (Exception e) {
      log.warn("Failed to encrypt anonymous user password, using password as is.", e);
      document.field(P_ANONYMOUS_PASSWORD, entity.getAnonymousPassword());
    }
    document.field(P_REALMS, entity.getRealms());

    return document.save();
  }

  /**
   * Read entity from document.
   */
  public SecuritySettings read(final ODocument document) {
    checkNotNull(document);

    boolean anonymousEnabled = document.field(P_ANONYMOUS_ACCESS_ENABLED, OType.BOOLEAN);
    String anonymousUsername = document.field(P_ANONYMOUS_USERNAME, OType.STRING);
    String anonymousPassword = document.field(P_ANONYMOUS_PASSWORD, OType.STRING);
    List<String> realms = document.field(P_REALMS, OType.EMBEDDEDLIST);

    SecuritySettings entity = new SecuritySettings();
    entity.setAnonymousAccessEnabled(anonymousEnabled);
    entity.setAnonymousUsername(anonymousUsername);
    try {
      entity.setAnonymousPassword(passwordHelper.decrypt(anonymousPassword));
    }
    catch (Exception e) {
      log.warn("Failed to decrypt anonymous user password, using password as is.", e);
      entity.setAnonymousPassword(anonymousPassword);
    }
    entity.setRealms(realms);

    return entity;
  }

  /**
   * Browse all documents.
   */
  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(DB_CLASS);
  }
}
