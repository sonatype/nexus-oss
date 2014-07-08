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
package org.sonatype.nexus.coreui

import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.proxy.repository.ProxyMode

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * Repository Proxy exchange object.
 *
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class RepositoryProxyXO
extends RepositoryXO
{
  ProxyMode proxyMode
  String remoteStatus
  String remoteStatusReason
  String remoteStorageUrl
  Boolean useTrustStoreForRemoteStorageUrl

  @NotNull
  Boolean autoBlockActive

  @NotNull
  Boolean fileTypeValidation

  Boolean authEnabled

  @NotEmpty(groups = Authentication)
  String authUsername

  Password authPassword
  String authNtlmHost
  String authNtlmDomain

  Boolean httpRequestSettings
  String userAgentCustomisation
  String urlParameters

  @Min(value = 0L, groups = HttpRequestSettings)
  @Max(value = 3600L, groups = HttpRequestSettings)
  Integer timeout

  @Min(value = 0L, groups = HttpRequestSettings)
  @Max(value = 10L, groups = HttpRequestSettings)
  Integer retries

  @Min(-1L)
  @Max(511000L)
  Integer notFoundCacheTTL

  @Min(-1L)
  @Max(511000L)
  Integer itemMaxAge

  public interface Authentication
  {}

  public interface HttpRequestSettings
  {}
}
