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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.sonatype.nexus.repository.nuget.security.NugetApiKeyStore;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.security.UserPrincipalsHelper;
import org.sonatype.nexus.security.user.UserNotFoundException;

import com.google.common.base.Charsets;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.apache.shiro.subject.PrincipalCollection;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;

/**
 * OrientDB impl of {@link NugetApiKeyStore}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class NugetApiKeyStoreImpl
    extends StateGuardLifecycleSupport
    implements NugetApiKeyStore
{
  private final Provider<DatabaseInstance> databaseInstance;

  private final NugetApiKeyEntityAdapter entityAdapter;

  private final UserPrincipalsHelper principalsHelper;

  private final Random random = new Random();

  @Inject
  public NugetApiKeyStoreImpl(final @Named("security") Provider<DatabaseInstance> databaseInstance,
                              final NugetApiKeyEntityAdapter entityAdapter, final UserPrincipalsHelper principalsHelper)
  {
    this.databaseInstance = checkNotNull(databaseInstance);
    this.entityAdapter = checkNotNull(entityAdapter);
    this.principalsHelper = checkNotNull(principalsHelper);
  }

  @Override
  protected void doStart() throws Exception {
    try (ODatabaseDocumentTx db = databaseInstance.get().connect()) {
      entityAdapter.register(db);
    }
  }

  private ODatabaseDocumentTx openDb() {
    return databaseInstance.get().acquire();
  }

  @Override
  @Guarded(by = STARTED)
  public char[] createApiKey(final PrincipalCollection principals) {
    checkNotNull(principals);

    final char[] apiKeyCharArray = makeApiKey(principals);

    final NugetApiKeyEntity entity = new NugetApiKeyEntity();

    entity.setApiKey(apiKeyCharArray);
    entity.setPrincipals(principals);

    try (ODatabaseDocumentTx db = openDb()) {
      entityAdapter.add(db, entity);
    }

    return apiKeyCharArray;
  }

  @Override
  @Guarded(by = STARTED)
  public char[] getApiKey(final PrincipalCollection principals) {
    try (ODatabaseDocumentTx db = openDb()) {
      final NugetApiKeyEntity entity = find(db, principals);

      return entity == null ? null : entity.getApiKey();
    }
  }

  @Override
  @Guarded(by = STARTED)
  public PrincipalCollection getPrincipals(final char[] apiKey) {
    try (ODatabaseDocumentTx db = openDb()) {
      final NugetApiKeyEntity entity = entityAdapter.findByApiKey(db, checkNotNull(apiKey));

      return entity == null ? null : entity.getPrincipals();
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void deleteApiKey(final PrincipalCollection principals) {
    try (ODatabaseDocumentTx db = openDb()) {
      final NugetApiKeyEntity nugetApiKeyEntity = find(db, principals);
      if (nugetApiKeyEntity != null) {
        entityAdapter.delete(db, nugetApiKeyEntity);
      }
    }
  }

  @Override
  @Guarded(by = STARTED)
  public void purgeApiKeys() {
    try (ODatabaseDocumentTx db = openDb()) {

      List<NugetApiKeyEntity> delete = new ArrayList<>();

      for (NugetApiKeyEntity entity : entityAdapter.browse(db)) {
        try {
          principalsHelper.getUserStatus(entity.getPrincipals());
        }
        catch (UserNotFoundException e) {
          delete.add(entity);
        }
      }

      for (NugetApiKeyEntity entity : delete) {
        entityAdapter.delete(db, entity);
      }
    }
  }

  private NugetApiKeyEntity find(final ODatabaseDocumentTx db, final PrincipalCollection principals) {
    final String primaryPrincipal = checkNotNull(principals).getPrimaryPrincipal().toString();
    return entityAdapter.findByPrimaryPrincipal(db, primaryPrincipal);
  }

  private char[] makeApiKey(final PrincipalCollection principals) {
    final String salt = new BigInteger(32, random).toString(32);
    final byte[] code = ("~NuGet~" + principals + salt).getBytes(Charsets.UTF_8);
    final String apiKey = UUID.nameUUIDFromBytes(code).toString();
    return apiKey.toCharArray();
  }
}
