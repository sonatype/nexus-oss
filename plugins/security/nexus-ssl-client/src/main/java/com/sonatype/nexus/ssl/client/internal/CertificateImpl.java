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

import com.sonatype.nexus.ssl.client.Certificate;
import com.sonatype.nexus.ssl.model.CertificatePemXO;
import com.sonatype.nexus.ssl.model.CertificateXO;

import org.sonatype.nexus.client.rest.support.EntitySupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Jersey based {@link com.sonatype.nexus.ssl.client.Certificate}.
 *
 * @since ssl 1.0
 */
public class CertificateImpl
    extends EntitySupport<Certificate, CertificateXO>
    implements Certificate
{

  private final TrustStoreImpl.Client client;

  public CertificateImpl(TrustStoreImpl.Client client) {
    super(null);
    this.client = checkNotNull(client, "client");
  }

  public CertificateImpl(TrustStoreImpl.Client client, CertificateXO settings) {
    super(settings.getId(), settings);
    this.client = checkNotNull(client, "client");
  }

  @Override
  public String id() {
    return settings().getId();
  }

  @Override
  protected CertificateXO createSettings(final String id) {
    final CertificateXO resource = new CertificateXO();
    resource.setId(id);
    return resource;
  }

  @Override
  protected CertificateXO doGet() {
    return client.get(id());
  }

  @Override
  protected CertificateXO doCreate() {
    return doUpdate();
  }

  @Override
  protected CertificateXO doUpdate() {
    return client.update(new CertificatePemXO().withValue(settings().getPem()));
  }

  @Override
  protected void doRemove() {
    client.delete(id());
  }

  @Override
  public String fingerprint() {
    return settings().getFingerprint();
  }

  @Override
  public String pem() {
    return settings().getPem();
  }

  @Override
  public Certificate withPem(final String pem) {
    settings().withPem(pem);
    // TODO Shall we calculate it?
    settings().withFingerprint(null);
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CertificateImpl)) {
      return false;
    }

    final CertificateImpl
        that = (CertificateImpl) o;

    if (id() != null ? !id().equals(that.id()) : that.id() != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return id() != null ? id().hashCode() : 0;
  }

}
