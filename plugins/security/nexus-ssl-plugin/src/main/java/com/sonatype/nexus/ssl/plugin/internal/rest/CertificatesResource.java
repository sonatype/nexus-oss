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
package com.sonatype.nexus.ssl.plugin.internal.rest;

import java.net.URL;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sonatype.nexus.ssl.model.CertificatePemXO;
import com.sonatype.nexus.ssl.model.CertificateXO;
import com.sonatype.nexus.ssl.plugin.SSLPlugin;
import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.nexus.ssl.plugin.internal.CertificateRetriever;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.siesta.Resource;
import org.sonatype.siesta.ValidationErrorsException;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ssl.plugin.internal.rest.TrustStoreResource.asCertificateXO;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.sonatype.sisu.goodies.ssl.keystore.CertificateUtil.calculateFingerprint;
import static org.sonatype.sisu.goodies.ssl.keystore.CertificateUtil.decodePEMFormattedCertificate;

/**
 * Certificate resource.
 *
 * @since ssl 1.0
 */
@Named
@Singleton
@Path(SSLPlugin.REST_PREFIX + "/certificates")
public class CertificatesResource
    extends ComponentSupport
    implements Resource
{

  private final RepositoryRegistry repositoryRegistry;

  private final TrustStore trustStore;

  private final ApplicationConfiguration applicationConfiguration;

  private final CertificateRetriever certificateRetriever;

  @Inject
  public CertificatesResource(final CertificateRetriever certificateRetriever,
                              final ApplicationConfiguration applicationConfiguration,
                              final RepositoryRegistry repositoryRegistry,
                              final TrustStore trustStore)
  {
    this.certificateRetriever = checkNotNull(certificateRetriever);
    this.applicationConfiguration = checkNotNull(applicationConfiguration);
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
    this.trustStore = checkNotNull(trustStore);
  }

  @GET
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:read")
  public Object get(final @QueryParam("repositoryId") String repositoryId,
                    final @QueryParam("host") String host,
                    final @QueryParam("port") String port,
                    final @QueryParam("protocolHint") String protocolHint)
      throws Exception
  {
    RemoteStorageContext remoteStorageContext = applicationConfiguration.getGlobalRemoteStorageContext();
    String actualProtocolHint = protocolHint;
    String actualHost = host;
    String actualPort = port;
    if (repositoryId != null) {
      final ProxyRepository repository = repositoryRegistry.getRepositoryWithFacet(
          repositoryId, ProxyRepository.class
      );
      final URL url = new URL(repository.getRemoteUrl());
      actualHost = url.getHost();
      actualPort = String.valueOf(url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
      remoteStorageContext = repository.getRemoteStorageContext();
      if (actualProtocolHint == null) {
        actualProtocolHint = "https";
      }
    }
    if (actualHost != null) {
      int actualPortInt = 443;
      if (actualPort != null) {
        try {
          actualPortInt = Integer.valueOf(actualPort);
        }
        catch (NumberFormatException e) {
          throw new ValidationErrorsException("port", "Port must be an integer");
        }
      }

      Certificate[] chain;
      try {
        chain = retrieveCertificates(remoteStorageContext, actualHost, actualPortInt, actualProtocolHint);
      }
      catch (Exception e) {
        String errorMessage = e.getMessage();
        if (e instanceof UnknownHostException) {
          errorMessage = "Unknown host '" + actualHost + "'";
        }
        throw new NotFoundException(errorMessage);
      }
      if (chain == null || chain.length == 0) {
        throw new NotFoundException(
            "Could not retrieve an SSL certificate from " + actualHost + ":" + actualPortInt
        );
      }

      return asCertificateXO(chain[0], isInNexusSSLTrustStore(chain[0]));
    }
    throw new ValidationErrorsException("One of repositoryId or host/port should be specified");
  }

  private Certificate[] retrieveCertificates(final RemoteStorageContext remoteStorageContext,
                                             final String host,
                                             final int port,
                                             final String protocolHint)
      throws Exception
  {
    if (protocolHint == null) {
      try {
        return certificateRetriever.retrieveCertificates(host, port);
      }
      catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.info(
              "Cannot connect directly to {}:{}. Will retry using https protocol.", host, port, e
          );
        }
        else {
          log.info(
              "Cannot connect directly to {}:{} (got {}/{}). Will retry using https protocol.",
              host, port, e.getClass().getName(), e.getMessage()
          );
        }
        return certificateRetriever.retrieveCertificatesFromHttpsServer(host, port, remoteStorageContext);
      }
    }
    else if ("https".equalsIgnoreCase(protocolHint)) {
      return certificateRetriever.retrieveCertificatesFromHttpsServer(host, port, remoteStorageContext);
    }
    return certificateRetriever.retrieveCertificates(host, port);
  }

  @POST
  @Path("/details")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:read")
  public CertificateXO details(final CertificatePemXO pem)
      throws Exception
  {
    try {
      final Certificate certificate = decodePEMFormattedCertificate(pem.getValue());
      return asCertificateXO(certificate, isInNexusSSLTrustStore(certificate));
    }
    catch (CertificateParsingException e) {
      throw new ValidationErrorsException("pem", "Invalid PEM formatted certificate");
    }
  }

  private boolean isInNexusSSLTrustStore(final Certificate certificate) {
    try {
      return trustStore.getTrustedCertificate(calculateFingerprint(certificate)) != null;
    }
    catch (Exception ignore) {
      return false;
    }
  }

}
