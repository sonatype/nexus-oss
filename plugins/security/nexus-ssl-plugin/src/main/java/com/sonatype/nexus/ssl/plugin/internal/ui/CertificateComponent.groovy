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
package com.sonatype.nexus.ssl.plugin.internal.ui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import com.sonatype.nexus.ssl.model.CertificatePemXO
import com.sonatype.nexus.ssl.model.CertificateXO
import com.sonatype.nexus.ssl.plugin.internal.rest.CertificatesResource
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.validation.Validate

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * SSL Certificate {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'ssl_Certificate')
class CertificateComponent
extends DirectComponentSupport
{

  @Inject
  CertificatesResource certificatesResource

  /**
   * Retrieves certificate given a host/port.
   * @param host to get certificate from
   * @param port
   * @param protocolHint
   * @return certificate
   */
  @DirectMethod
  @Validate
  CertificateXO retrieveFromHost(final @NotEmpty(message = '[host] may not be empty') String host,
                                 final @Nullable Integer port,
                                 final @Nullable String protocolHint)
  {
    return certificatesResource.get(null, host, port as String, protocolHint) as CertificateXO
  }

  /**
   * Retrieves certificate given a certificate pem.
   * @param pem to get details from
   * @return certificate
   */
  @DirectMethod
  @Validate
  CertificateXO details(final @NotNull(message = '[pem] may not be null') @Valid CertificatePemXO pem) {
    return certificatesResource.details(pem)
  }

}
