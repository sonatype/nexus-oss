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
package com.sonatype.nexus.repository.nuget.internal.security

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import com.sonatype.nexus.repository.nuget.security.NugetApiKeyStore
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.security.SecurityHelper
import org.sonatype.nexus.validation.Validate
import org.sonatype.nexus.wonderland.AuthTicketService

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Managing per-user NuGet Api Keys.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'nuget_NuGetApiKey')
class NugetApiKeyComponent
    extends DirectComponentSupport
{

  @Inject
  Provider<NugetApiKeyStore> keyStore

  @Inject
  AuthTicketService authTokens

  @Inject
  SecurityHelper securityHelper

  /**
   * Read NuGet API Key for current signed on user.
   */
  @DirectMethod
  @RequiresPermissions('nexus:apikey:read')
  @Validate
  String readKey(final @NotEmpty String authToken) {
    validateAuthToken(authToken)

    def principals = securityHelper.subject().principals
    char[] apiKey = keyStore.get().getApiKey(principals)
    if (!apiKey) {
      apiKey = keyStore.get().createApiKey(principals)
    }
    return new String(apiKey)
  }

  /**
   * Resets NuGet API Key for current signed on user.
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:apikey:delete')
  @Validate
  String resetKey(final @NotEmpty String authToken) {
    validateAuthToken(authToken)

    def principals = securityHelper.subject().principals
    def keyStore = keyStore.get()
    keyStore.deleteApiKey(principals)
    char[] apiKey = keyStore.createApiKey(principals)
    return new String(apiKey)
  }

  private void validateAuthToken(final String authToken) {
    assert authToken, 'Missing authentication ticket'

    log.debug 'Validating authentication ticket: {}', authToken

    if (!authTokens.redeemTicket(authToken)) {
      throw new IllegalAccessException('Invalid authentication ticket')
    }
  }
}
