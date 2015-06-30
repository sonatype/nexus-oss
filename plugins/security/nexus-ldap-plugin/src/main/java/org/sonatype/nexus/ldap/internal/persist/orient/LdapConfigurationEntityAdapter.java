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
package org.sonatype.nexus.ldap.internal.persist.orient;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ldap.internal.persist.entity.Connection;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;
import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link LdapConfiguration} entity adapter.
 *
 * @since 3.0
 */
@Named
@Singleton
public class LdapConfigurationEntityAdapter
    extends ComponentSupport
{
  private static final String DB_CLASS = new OClassNameBuilder()
      .prefix("ldap")
      .type(LdapConfiguration.class)
      .build();

  private static final String DB_CONNECTION_HOST_CLASS = new OClassNameBuilder()
      .prefix("ldap")
      .type(Host.class)
      .build();

  private static final String DB_CONNECTION_CLASS = new OClassNameBuilder()
      .prefix("ldap")
      .type(Connection.class)
      .build();

  private static final String DB_MAPPING_CLASS = new OClassNameBuilder()
      .prefix("ldap")
      .type(Mapping.class)
      .build();

  private static final String P_ID = "id";

  private static final String P_NAME = "name";

  private static final String P_ORDER = "order";

  private static final String P_CONNECTION = "connection";

  private static final String P_MAPPING = "mapping";

  private static final String I_ID = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_ID)
      .build();

  private static final String I_NAME = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_NAME)
      .build();

  public OClass register(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    OSchema schema = db.getMetadata().getSchema();
    OClass type = schema.getClass(DB_CLASS);
    if (type == null) {
      // connection
      OClass connectionHostType = schema.createClass(DB_CONNECTION_HOST_CLASS);
      connectionHostType.createProperty("protocol", OType.STRING).setNotNull(true);
      connectionHostType.createProperty("hostName", OType.STRING).setNotNull(true);
      connectionHostType.createProperty("port", OType.INTEGER).setNotNull(true);
      OClass connectionType = schema.createClass(DB_CONNECTION_CLASS);
      connectionType.createProperty("searchBase", OType.STRING).setNotNull(true);
      connectionType.createProperty("systemUsername", OType.STRING);
      connectionType.createProperty("systemPassword", OType.STRING);
      connectionType.createProperty("authScheme", OType.STRING).setNotNull(true);
      connectionType.createProperty("host", OType.EMBEDDED, connectionHostType).setNotNull(true);
      connectionType.createProperty("saslRealm", OType.STRING);
      connectionType.createProperty("connectionTimeout", OType.INTEGER).setNotNull(true);
      connectionType.createProperty("connectionRetryDelay", OType.INTEGER).setNotNull(true);
      connectionType.createProperty("maxIncidentsCount", OType.INTEGER).setNotNull(true);

      // mapping
      OClass mappingType = schema.createClass(DB_MAPPING_CLASS);
      mappingType.createProperty("emailAddressAttribute", OType.STRING).setNotNull(true);
      mappingType.createProperty("ldapGroupsAsRoles", OType.BOOLEAN).setNotNull(true);
      mappingType.createProperty("groupBaseDn", OType.STRING).setNotNull(true);
      mappingType.createProperty("groupIdAttribute", OType.STRING).setNotNull(true);
      mappingType.createProperty("groupMemberAttribute", OType.STRING).setNotNull(true);
      mappingType.createProperty("groupMemberFormat", OType.STRING).setNotNull(true);
      mappingType.createProperty("groupObjectClass", OType.STRING).setNotNull(true);
      mappingType.createProperty("userPasswordAttribute", OType.STRING).setNotNull(true);
      mappingType.createProperty("userIdAttribute", OType.STRING).setNotNull(true);
      mappingType.createProperty("userObjectClass", OType.STRING).setNotNull(true);
      mappingType.createProperty("ldapFilter", OType.STRING).setNotNull(true);
      mappingType.createProperty("userBaseDn", OType.STRING).setNotNull(true);
      mappingType.createProperty("userRealNameAttribute", OType.STRING).setNotNull(true);
      mappingType.createProperty("userSubtree", OType.BOOLEAN).setNotNull(true);
      mappingType.createProperty("groupSubtree", OType.BOOLEAN).setNotNull(true);
      mappingType.createProperty("userMemberOfAttribute", OType.STRING).setNotNull(false);

      // ldapConfiguration
      type = schema.createClass(DB_CLASS);
      type.createProperty(P_ID, OType.STRING).setNotNull(true);
      type.createProperty(P_NAME, OType.STRING).setNotNull(true);
      type.createProperty(P_ORDER, OType.INTEGER).setNotNull(true);
      type.createProperty(P_CONNECTION, OType.EMBEDDED, connectionType).setNotNull(true);
      type.createProperty(P_MAPPING, OType.EMBEDDED, mappingType).setNotNull(true);

      type.createIndex(I_ID, INDEX_TYPE.UNIQUE, P_ID);
      type.createIndex(I_NAME, INDEX_TYPE.UNIQUE, P_NAME);

      log.info("Created schema: {}, properties: {}", type, type.properties());
    }
    return type;
  }

  public ODocument selectById(final ODatabaseDocumentTx db, final String id) {
    checkNotNull(db);
    checkNotNull(id);
    final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>("SELECT FROM " + DB_CLASS + " WHERE " + P_ID + " = ?");
    final List<ODocument> results = db.command(query).execute(id);
    if (!results.isEmpty()) {
      return results.get(0);
    }
    return null;
  }

  public Iterable<ODocument> browse(final ODatabaseDocumentTx db) {
    checkNotNull(db);
    return db.browseClass(DB_CLASS);
  }

  public LdapConfiguration read(final ODocument document) {
    checkNotNull(document);
    LdapConfiguration entity = new LdapConfiguration();
    entity.setId(document.<String>field(P_ID, OType.STRING));
    entity.setName(document.<String>field(P_NAME, OType.STRING));
    entity.setOrder(document.<Integer>field(P_ORDER, OType.INTEGER));
    entity.setConnection(toConnection(document.<ODocument>field(P_CONNECTION, OType.EMBEDDED)));
    entity.setMapping(toMapping(document.<ODocument>field(P_MAPPING, OType.EMBEDDED)));
    return entity;
  }

  public ODocument edit(final ODatabaseDocumentTx db, final ODocument document, final LdapConfiguration entity) {
    checkNotNull(db);
    checkNotNull(document);
    checkNotNull(entity);

    document.field(P_ID, entity.getId());
    document.field(P_NAME, entity.getName());
    document.field(P_ORDER, entity.getOrder());
    document.field(P_CONNECTION, fromConnection(db, db.newInstance(DB_CONNECTION_CLASS), entity.getConnection()));
    document.field(P_MAPPING, fromMapping(db, db.newInstance(DB_MAPPING_CLASS), entity.getMapping()));

    return document.save();
  }

  public ODocument add(final ODatabaseDocumentTx db, final LdapConfiguration entity) {
    checkNotNull(db);
    return edit(db, db.newInstance(DB_CLASS), entity);
  }

  // ==

  private Connection toConnection(final ODocument document) {
    final Connection connection = new Connection();
    connection.setSearchBase(document.<String>field("searchBase", OType.STRING));
    connection.setSystemUsername(document.<String>field("systemUsername", OType.STRING));
    connection.setSystemPassword(document.<String>field("systemPassword", OType.STRING));
    connection.setAuthScheme(document.<String>field("authScheme", OType.STRING));
    final ODocument hostDocument = document.field("host", OType.EMBEDDED);
    final Host host = new Host(
        Protocol.valueOf(hostDocument.<String>field("protocol", OType.STRING)),
        hostDocument.<String>field("hostName", OType.STRING),
        hostDocument.<Integer>field("port", OType.INTEGER)
    );
    connection.setHost(host);
    connection.setUseTrustStore(document.<Boolean>field("useTrustStore", OType.BOOLEAN));
    connection.setSaslRealm(document.<String>field("saslRealm", OType.STRING));
    connection.setConnectionTimeout(document.<Integer>field("connectionTimeout", OType.INTEGER));
    connection.setConnectionRetryDelay(document.<Integer>field("connectionRetryDelay", OType.INTEGER));
    connection.setMaxIncidentsCount(document.<Integer>field("maxIncidentsCount", OType.INTEGER));
    return connection;
  }

  private ODocument fromConnection(final ODatabaseDocumentTx db,
                                   final ODocument document,
                                   final Connection connection)
  {
    document.field("searchBase", connection.getSearchBase());
    document.field("systemUsername", connection.getSystemUsername());
    document.field("systemPassword", connection.getSystemPassword());
    document.field("authScheme", connection.getAuthScheme());
    final ODocument hostDocument = db.newInstance(DB_CONNECTION_HOST_CLASS);
    hostDocument.field("protocol", connection.getHost().getProtocol().name());
    hostDocument.field("hostName", connection.getHost().getHostName());
    hostDocument.field("port", connection.getHost().getPort());
    document.field("host", hostDocument);
    document.field("useTrustStore", connection.getUseTrustStore());
    document.field("saslRealm", connection.getSaslRealm());
    document.field("connectionTimeout", connection.getConnectionTimeout());
    document.field("connectionRetryDelay", connection.getConnectionRetryDelay());
    document.field("maxIncidentsCount", connection.getMaxIncidentsCount());
    return document;
  }

  private Mapping toMapping(final ODocument document) {
    final Mapping mapping = new Mapping();
    mapping.setEmailAddressAttribute(document.<String>field("emailAddressAttribute", OType.STRING));
    mapping.setLdapGroupsAsRoles(document.<Boolean>field("ldapGroupsAsRoles", OType.BOOLEAN));
    mapping.setGroupBaseDn(document.<String>field("groupBaseDn", OType.STRING));
    mapping.setGroupIdAttribute(document.<String>field("groupIdAttribute", OType.STRING));
    mapping.setGroupMemberAttribute(document.<String>field("groupMemberAttribute", OType.STRING));
    mapping.setGroupMemberFormat(document.<String>field("groupMemberFormat", OType.STRING));
    mapping.setGroupObjectClass(document.<String>field("groupObjectClass", OType.STRING));
    mapping.setUserPasswordAttribute(document.<String>field("userPasswordAttribute", OType.STRING));
    mapping.setUserIdAttribute(document.<String>field("userIdAttribute", OType.STRING));
    mapping.setUserObjectClass(document.<String>field("userObjectClass", OType.STRING));
    mapping.setLdapFilter(document.<String>field("ldapFilter", OType.STRING));
    mapping.setUserBaseDn(document.<String>field("userBaseDn", OType.STRING));
    mapping.setUserRealNameAttribute(document.<String>field("userRealNameAttribute", OType.STRING));
    mapping.setUserSubtree(document.<Boolean>field("userSubtree", OType.BOOLEAN));
    mapping.setGroupSubtree(document.<Boolean>field("groupSubtree", OType.BOOLEAN));
    mapping.setUserMemberOfAttribute(document.<String>field("userMemberOfAttribute", OType.STRING));
    return mapping;
  }

  private ODocument fromMapping(final ODatabaseDocumentTx db,
                                final ODocument document,
                                final Mapping mapping)
  {
    document.field("emailAddressAttribute", mapping.getEmailAddressAttribute());
    document.field("ldapGroupsAsRoles", mapping.isLdapGroupsAsRoles());
    document.field("groupBaseDn", mapping.getGroupBaseDn());
    document.field("groupIdAttribute", mapping.getGroupIdAttribute());
    document.field("groupMemberAttribute", mapping.getGroupMemberAttribute());
    document.field("groupMemberFormat", mapping.getGroupMemberFormat());
    document.field("groupObjectClass", mapping.getGroupObjectClass());
    document.field("userPasswordAttribute", mapping.getUserPasswordAttribute());
    document.field("userIdAttribute", mapping.getUserIdAttribute());
    document.field("userObjectClass", mapping.getUserObjectClass());
    document.field("ldapFilter", mapping.getLdapFilter());
    document.field("userBaseDn", mapping.getUserBaseDn());
    document.field("userRealNameAttribute", mapping.getUserRealNameAttribute());
    document.field("userSubtree", mapping.isUserSubtree());
    document.field("groupSubtree", mapping.isGroupSubtree());
    document.field("userMemberOfAttribute", mapping.getUserMemberOfAttribute());
    return document;
  }

}
