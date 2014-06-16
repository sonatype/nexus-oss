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

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;

import com.sonatype.nexus.ssl.model.CertificatePemXO;
import com.sonatype.nexus.ssl.model.CertificateXO;
import com.sonatype.nexus.ssl.plugin.SSLPlugin;
import com.sonatype.nexus.ssl.plugin.TrustStore;

import org.sonatype.siesta.Resource;
import org.sonatype.siesta.ValidationErrorsException;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.ssl.keystore.CertificateUtil;
import org.sonatype.sisu.goodies.ssl.keystore.internal.geronimo.KeyNotFoundException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.sonatype.sisu.goodies.ssl.keystore.CertificateUtil.calculateFingerprint;

/**
 * Trust store resource.
 *
 * @since ssl 1.0
 */
@Named
@Singleton
@Path(SSLPlugin.REST_PREFIX + "/truststore")
public class TrustStoreResource
    extends ComponentSupport
    implements Resource
{

  private final TrustStore trustStore;

  @Inject
  public TrustStoreResource(final TrustStore trustStore) {
    this.trustStore = checkNotNull(trustStore);
  }

  @GET
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:read")
  public GenericEntity<List<CertificateXO>> get()
      throws Exception
  {
    final List<CertificateXO> certificateXOs = Lists.newArrayList();

    final Collection<Certificate> certificates = trustStore.getTrustedCertificates();
    if (certificates != null) {
      for (final Certificate certificate : certificates) {
        certificateXOs.add(asCertificateXO(certificate, true));
      }
    }

    return new GenericEntity<List<CertificateXO>>(certificateXOs)
    {
      @Override
      public String toString() {
        return getEntity().toString();
      }
    };
  }

  @POST
  @Consumes({APPLICATION_XML, APPLICATION_JSON})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:create")
  public CertificateXO create(final CertificatePemXO pem)
      throws Exception
  {
    try {
      final Certificate certificate = CertificateUtil.decodePEMFormattedCertificate(pem.getValue());
      trustStore.importTrustCertificate(certificate, calculateFingerprint(certificate));
      return asCertificateXO(certificate, true);
    }
    catch (CertificateParsingException e) {
      throw new ValidationErrorsException("pem", "Invalid PEM formatted certificate");
    }
  }

  @GET
  @Path("/{id}")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:read")
  public CertificateXO getByFingerprint(final @PathParam("id") String id)
      throws Exception
  {
    try {
      final Certificate certificate = trustStore.getTrustedCertificate(id);
      return asCertificateXO(certificate, true);
    }
    catch (KeyNotFoundException e) {
      throw new NotFoundException(format("Certificate with fingerprint '%s' not found", id));
    }
  }

  @DELETE
  @Path("/{id}")
  @RequiresPermissions(SSLPlugin.PERMISSION_PREFIX + "truststore:delete")
  public void delete(final @PathParam("id") String id)
      throws Exception
  {
    try {
      trustStore.removeTrustCertificate(id);
    }
    catch (KeyNotFoundException e) {
      throw new NotFoundException(format("Certificate with fingerprint '%s' not found", id));
    }
  }

  public static CertificateXO asCertificateXO(final Certificate certificate, final boolean inNexusSSLTrustStore)
      throws Exception
  {
    final String fingerprint = calculateFingerprint(certificate);

    final CertificateXO certificateXO = new CertificateXO()
        .withId(fingerprint)
        .withPem(CertificateUtil.serializeCertificateInPEM(certificate))
        .withFingerprint(fingerprint);

    if (certificate instanceof X509Certificate) {
      final X509Certificate x509Certificate = (X509Certificate) certificate;

      final Map<String, String> subjectRdns = getSubjectRdns(certificate);
      final Map<String, String> issuerRdns = getIssuerRdns(certificate);

      certificateXO
          .withSerialNumber(x509Certificate.getSerialNumber().toString())
          .withSubjectCommonName(subjectRdns.get("CN"))
          .withSubjectOrganization(subjectRdns.get("O"))
          .withSubjectOrganizationalUnit(subjectRdns.get("OU"))
          .withIssuerCommonName(issuerRdns.get("CN"))
          .withIssuerOrganization(issuerRdns.get("O"))
          .withIssuerOrganizationalUnit(issuerRdns.get("OU"))
          .withIssuedOn(x509Certificate.getNotBefore().getTime())
          .withExpiresOn(x509Certificate.getNotAfter().getTime())
          .withInNexusSSLTrustStore(inNexusSSLTrustStore);
    }

    return certificateXO;
  }

  private static Map<String, String> getSubjectRdns(final Certificate certificate) {
    if (certificate instanceof X509Certificate) {
      return getRdns(((X509Certificate) certificate).getSubjectX500Principal().getName());
    }
    return Maps.newHashMap();
  }

  private static Map<String, String> getIssuerRdns(final Certificate certificate) {
    if (certificate instanceof X509Certificate) {
      return getRdns(((X509Certificate) certificate).getIssuerX500Principal().getName());
    }
    return Maps.newHashMap();
  }

  private static Map<String, String> getRdns(final String dn) {
    final Map<String, String> rdns = Maps.newHashMap();
    try {
      final LdapName ldapName = new LdapName(dn);
      for (final Rdn rdn : ldapName.getRdns()) {
        rdns.put(rdn.getType(), rdn.getValue().toString());
      }
    }
    catch (InvalidNameException e) {
      // TODO ?
    }
    return rdns;
  }

}
