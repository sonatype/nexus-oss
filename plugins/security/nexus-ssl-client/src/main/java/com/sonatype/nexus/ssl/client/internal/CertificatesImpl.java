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

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sonatype.nexus.ssl.client.Certificate;
import com.sonatype.nexus.ssl.client.Certificates;
import com.sonatype.nexus.ssl.model.CertificatePemXO;
import com.sonatype.nexus.ssl.model.CertificateXO;

import org.sonatype.nexus.client.core.subsystem.SiestaClient;
import org.sonatype.sisu.siesta.client.ClientBuilder.Target.Factory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Jersey based Certificates Nexus client subsystem implementation.
 *
 * @since ssl 1.0
 */
public class CertificatesImpl
    implements Certificates
{

  private final Client client;

  private final TrustStoreImpl.Client trustStoreClient;

  @Inject
  public CertificatesImpl(final Factory factory) {
    client = checkNotNull(factory, "factory").build(Client.class);
    trustStoreClient = factory.build(TrustStoreImpl.Client.class);
  }

  @Override
  public Certificate get(final String repositoryId) {
    return convert(client.get(checkNotNull(repositoryId)));
  }

  @Override
  public Certificate get(final String host, final int port) {
    return get(host, port, null);
  }

  @Override
  public Certificate get(final String host, final int port, final String protocolHint) {
    return convert(client.get(host, port > 0 ? port : null, protocolHint));
  }

  @Override
  public Certificate getDetails(final String pem) {
    checkNotNull(pem);

    return convert(client.details(new CertificatePemXO().withValue(pem)));
  }

  private Certificate convert(final CertificateXO resource) {
    if (resource == null) {
      return null;
    }
    return new CertificateImpl(trustStoreClient, resource);
  }

  @Path("/service/siesta/ssl/certificates")
  public static interface Client
      extends SiestaClient
  {

    @GET
    @Consumes({APPLICATION_JSON})
    CertificateXO get(@QueryParam("repositoryId") String repositoryId);

    @GET
    @Consumes({APPLICATION_JSON})
    CertificateXO get(@QueryParam("host") String host,
                      @QueryParam("port") Integer port,
                      @QueryParam("protocolHint") String protocolHint);

    @POST
    @Produces({APPLICATION_JSON})
    @Consumes({APPLICATION_JSON})
    @Path("/details")
    CertificateXO details(CertificatePemXO pem);

  }

}
