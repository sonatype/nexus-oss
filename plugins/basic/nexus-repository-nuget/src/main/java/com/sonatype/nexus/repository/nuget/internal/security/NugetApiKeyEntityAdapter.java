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
package com.sonatype.nexus.repository.nuget.internal.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.OClassNameBuilder;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.nexus.orient.entity.CollectionEntityAdapter;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.shiro.subject.PrincipalCollection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link NugetApiKeyEntity} entity adapter.
 *
 * since 3.0
 */
@Named
@Singleton
public class NugetApiKeyEntityAdapter
    extends CollectionEntityAdapter<NugetApiKeyEntity>
{
  private static final String DB_CLASS = new OClassNameBuilder()
      .type(NugetApiKeyEntityAdapter.class)
      .build();

  private static final String P_PRIMARY_PRINCIPAL = "primary_principal";

  private static final String P_APIKEY = "api_key";

  private static final String SELECT_BY_API_KEY = "SELECT FROM " + DB_CLASS + " WHERE " + P_APIKEY + "=?";

  private static final String SELECT_BY_PRIMARY_PRINCIPAL =
      "SELECT FROM " + DB_CLASS + " WHERE " + P_PRIMARY_PRINCIPAL + "=?";

  private static final String P_PRINCIPALS = "principals";

  private static final String I_APIKEY = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_APIKEY)
      .build();

  private static final String I_PRIMARY_PRINCIPAL = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_PRIMARY_PRINCIPAL)
      .build();

  public NugetApiKeyEntityAdapter() {
    super(DB_CLASS);
  }

  @Override
  protected void defineType(final OClass type) {
    type.createProperty(P_APIKEY, OType.STRING)
        .setMandatory(true)
        .setNotNull(true);
    type.createProperty(P_PRIMARY_PRINCIPAL, OType.STRING)
        .setMandatory(true)
        .setNotNull(true);
    type.createProperty(P_PRINCIPALS, OType.BINARY)
        .setMandatory(true)
        .setNotNull(true);
    type.createIndex(I_APIKEY, INDEX_TYPE.UNIQUE, P_APIKEY);
    type.createIndex(I_PRIMARY_PRINCIPAL, INDEX_TYPE.UNIQUE, P_PRIMARY_PRINCIPAL);
  }

  @Override
  protected NugetApiKeyEntity newEntity() {
    return new NugetApiKeyEntity();
  }

  @Override
  protected void readFields(final ODocument document, final NugetApiKeyEntity entity) {
    String apiKey = document.field(P_APIKEY, OType.STRING);
    final PrincipalCollection principals = (PrincipalCollection) deserialize(document, P_PRINCIPALS);

    entity.setApiKey(apiKey.toCharArray());
    entity.setPrincipals(principals);
  }

  @Override
  protected void writeFields(final ODocument document, final NugetApiKeyEntity entity) {
    document.field(P_APIKEY, String.valueOf(entity.getApiKey()));
    document.field(P_PRIMARY_PRINCIPAL, entity.getPrincipals().getPrimaryPrincipal().toString());
    document.field(P_PRINCIPALS, serialize(entity.getPrincipals()));
  }

  @Nullable
  public NugetApiKeyEntity findByApiKey(final ODatabaseDocumentTx db, final char[] apiKey) {
    final OResultSet<ODocument> resultSet = db
        .command(new OSQLSynchQuery<ODocument>(SELECT_BY_API_KEY))
        .execute(String.valueOf(checkNotNull(apiKey)));

    if (resultSet.isEmpty()) {
      return null;
    }

    return readEntity(resultSet.iterator().next());
  }

  public NugetApiKeyEntity findByPrimaryPrincipal(final ODatabaseDocumentTx db, final String principal) {
    final OResultSet<ODocument> resultSet = db
        .command(new OSQLSynchQuery<ODocument>(SELECT_BY_PRIMARY_PRINCIPAL))
        .execute(checkNotNull(principal));

    if (resultSet.isEmpty()) {
      return null;
    }

    return readEntity(resultSet.iterator().next());
  }

  private Object deserialize(final ODocument document, final String fieldName) {
    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
    final byte[] bytes = document.field(fieldName, OType.BINARY);
    try (ObjectInputStream objects = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      return objects.readObject();
    }
    catch (IOException | ClassNotFoundException e) {
      throw Throwables.propagate(e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(tccl);
    }
  }

  private byte[] serialize(final Object object) {
    try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
      ObjectOutputStream objects = new ObjectOutputStream(bytes);
      objects.writeObject(object);
      objects.flush();
      return bytes.toByteArray();
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
