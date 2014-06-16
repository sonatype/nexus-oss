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
package com.sonatype.nexus.ssl.client.internal;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sonatype.nexus.ssl.client.Certificate;
import com.sonatype.nexus.ssl.client.TrustStore;
import com.sonatype.nexus.ssl.model.CertificatePemXO;
import com.sonatype.nexus.ssl.model.CertificateXO;
import com.sonatype.nexus.ssl.model.TrustStoreKey;
import com.sonatype.nexus.ssl.model.TrustStoreKeyXO;

import org.sonatype.nexus.client.core.subsystem.SiestaClient;
import org.sonatype.sisu.siesta.client.ClientBuilder.Target.Factory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Jersey based Trust Store Nexus client subsystem implementation.
 *
 * @since ssl 1.0
 */
public class TrustStoreImpl
    implements TrustStore
{

  private final Client client;

  @Inject
  public TrustStoreImpl(final Factory factory) {
    client = checkNotNull(factory, "factory").build(Client.class);
  }

  @Override
  public Certificate create() {
    return new CertificateImpl(client);
  }

  @Override
  public Collection<Certificate> get() {
    return Collections2.transform(client.get(), new Function<CertificateXO, Certificate>()
    {
      @Override
      public Certificate apply(@Nullable final CertificateXO input) {
        return convert(input);
      }
    });
  }

  @Override
  public Certificate get(final String id) {
    return convert(client.get(checkNotNull(id, "id")));
  }

  @Override
  public boolean isEnabledFor(final TrustStoreKey key) {
    return getTrustStoreKey(key);
  }

  @Override
  public TrustStoreImpl enableFor(final TrustStoreKey key) {
    return updateTrustStoreKey(key, true);
  }

  @Override
  public TrustStoreImpl disableFor(final TrustStoreKey key) {
    return updateTrustStoreKey(key, false);
  }

  private Certificate convert(final CertificateXO resource) {
    if (resource == null) {
      return null;
    }
    return new CertificateImpl(client, resource);
  }

  private boolean getTrustStoreKey(final TrustStoreKey key) {
    return client.getKey(checkNotNull(key).value()).isEnabled();
  }

  private TrustStoreImpl updateTrustStoreKey(final TrustStoreKey key, final boolean enabled) {
    client.updateKey(checkNotNull(key).value(), new TrustStoreKeyXO().withEnabled(enabled));
    return this;
  }

  @Path("/service/siesta/ssl/truststore")
  public static interface Client
      extends SiestaClient
  {

    @GET
    @Consumes({APPLICATION_JSON})
    List<CertificateXO> get();

    @GET
    @Consumes({APPLICATION_JSON})
    @Path("/{id}")
    CertificateXO get(@PathParam("id") String id);

    @POST
    @Produces({APPLICATION_JSON})
    @Consumes({APPLICATION_JSON})
    CertificateXO update(CertificatePemXO key);

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") String id);

    @GET
    @Consumes({APPLICATION_JSON})
    @Path("/key/{id}")
    TrustStoreKeyXO getKey(@PathParam("id") String id);

    @PUT
    @Produces({APPLICATION_JSON})
    @Consumes({APPLICATION_JSON})
    @Path("/key/{id}")
    void updateKey(@PathParam("id") String id, TrustStoreKeyXO key);

  }

}
