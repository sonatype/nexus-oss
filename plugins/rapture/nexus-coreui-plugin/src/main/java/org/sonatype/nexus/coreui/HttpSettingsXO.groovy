/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import javax.validation.constraints.Max
import javax.validation.constraints.Min

import org.sonatype.nexus.extdirect.model.Password

import groovy.transform.ToString

/**
 * HTTP System Settings exchange object.
 *
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class HttpSettingsXO
{
  String userAgentCustomisation
  String urlParameters

  /**
   * Timeout seconds.
   */
  @Min(0L)
  @Max(3600L) // 1 hour
  Integer timeout

  @Min(0L)
  @Max(10L)
  Integer retries

  Boolean httpEnabled
  String httpHost

  @Min(1L)
  @Max(65535L)
  Integer httpPort

  Boolean httpAuthEnabled
  String httpAuthUsername
  Password httpAuthPassword
  String httpAuthNtlmHost
  String httpAuthNtlmDomain

  Boolean httpsEnabled
  String httpsHost

  @Min(1L)
  @Max(65535L)
  Integer httpsPort

  Boolean httpsAuthEnabled
  String httpsAuthUsername
  Password httpsAuthPassword
  String httpsAuthNtlmHost
  String httpsAuthNtlmDomain

  Set<String> nonProxyHosts
}
