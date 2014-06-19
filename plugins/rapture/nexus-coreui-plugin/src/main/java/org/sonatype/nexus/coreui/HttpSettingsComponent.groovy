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

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.commons.lang.StringUtils
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings
import org.sonatype.nexus.configuration.application.GlobalRemoteProxySettings
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.guice.Validate
import org.sonatype.nexus.proxy.repository.*

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * HTTP System Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_HttpSettings')
class HttpSettingsComponent
extends DirectComponentSupport
{

  @Inject
  GlobalRemoteConnectionSettings connectionSettings

  @Inject
  GlobalRemoteProxySettings proxySettings

  @Inject
  NexusConfiguration nexusConfiguration

  /**
   * Retrieves HTTP system settings
   * @return HTTP system settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  HttpSettingsXO read() {
    def httpSettingsXO = new HttpSettingsXO(
        userAgentCustomisation: connectionSettings.userAgentCustomizationString,
        urlParameters: connectionSettings.queryString,
        timeout: connectionSettings.connectionTimeout == 0 ? 0 : connectionSettings.connectionTimeout / 1000,
        retries: connectionSettings.retrievalRetryCount,
        nonProxyHosts: proxySettings.nonProxyHosts
    )

    def (enabled, host, port, username, ntlmHost, ntlmDomain) = fromRemoteHttpProxySettings(proxySettings.httpProxySettings)
    httpSettingsXO.httpEnabled = enabled
    httpSettingsXO.httpHost = host
    httpSettingsXO.httpPort = port
    httpSettingsXO.httpAuthEnabled = StringUtils.isNotBlank(username as String)
    httpSettingsXO.httpAuthUsername = username
    httpSettingsXO.httpAuthPassword = httpSettingsXO.httpAuthEnabled ? Password.fakePassword() : null
    httpSettingsXO.httpAuthNtlmHost = ntlmHost
    httpSettingsXO.httpAuthNtlmDomain = ntlmDomain

    (enabled, host, port, username, ntlmHost, ntlmDomain) = fromRemoteHttpProxySettings(proxySettings.httpsProxySettings)
    httpSettingsXO.httpsEnabled = enabled
    httpSettingsXO.httpsHost = host
    httpSettingsXO.httpsPort = port
    httpSettingsXO.httpsAuthEnabled = StringUtils.isNotBlank(username as String)
    httpSettingsXO.httpsAuthUsername = username
    httpSettingsXO.httpAuthPassword = httpSettingsXO.httpsAuthEnabled ? Password.fakePassword() : null
    httpSettingsXO.httpsAuthNtlmHost = ntlmHost
    httpSettingsXO.httpsAuthNtlmDomain = ntlmDomain

    return httpSettingsXO
  }

  /**
   * Updates HTTP system settings .
   * @return updated HTTP system settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  HttpSettingsXO update(final @NotNull(message = '[httpSettingsXO] may not be null') @Valid HttpSettingsXO httpSettingsXO) {
    connectionSettings.userAgentCustomizationString = httpSettingsXO.userAgentCustomisation
    connectionSettings.queryString = httpSettingsXO.urlParameters
    connectionSettings.connectionTimeout = httpSettingsXO.timeout * 1000
    connectionSettings.retrievalRetryCount = httpSettingsXO.retries

    proxySettings.nonProxyHosts = httpSettingsXO.httpEnabled ? httpSettingsXO.nonProxyHosts : null
    proxySettings.httpProxySettings = toRemoteHttpProxySettings(
        httpSettingsXO.httpEnabled, httpSettingsXO.httpHost, httpSettingsXO.httpPort,
        httpSettingsXO.httpAuthEnabled,
        httpSettingsXO.httpAuthUsername, getPassword(httpSettingsXO.httpAuthPassword, proxySettings.httpProxySettings?.proxyAuthentication),
        httpSettingsXO.httpAuthNtlmHost, httpSettingsXO.httpAuthNtlmDomain
    )
    proxySettings.httpsProxySettings = toRemoteHttpProxySettings(
        httpSettingsXO.httpsEnabled, httpSettingsXO.httpsHost, httpSettingsXO.httpsPort,
        httpSettingsXO.httpsAuthEnabled,
        httpSettingsXO.httpsAuthUsername, getPassword(httpSettingsXO.httpsAuthPassword, proxySettings.httpsProxySettings?.proxyAuthentication),
        httpSettingsXO.httpsAuthNtlmHost, httpSettingsXO.httpsAuthNtlmDomain
    )

    nexusConfiguration.saveConfiguration()
    return read()
  }

  private static fromRemoteHttpProxySettings(final RemoteHttpProxySettings settings) {
    def enabled = false, hostname = null, port = null, username = null, ntlmHost = null, ntlmDomain = null
    if (settings) {
      enabled = settings.enabled
      if (enabled) {
        hostname = settings.hostname
        port = settings.port
        if (settings.proxyAuthentication) {
          if (settings.proxyAuthentication instanceof UsernamePasswordRemoteAuthenticationSettings) {
            def auth = (UsernamePasswordRemoteAuthenticationSettings) settings.proxyAuthentication
            username = auth.username
          }
          if (settings.proxyAuthentication instanceof NtlmRemoteAuthenticationSettings) {
            def auth = (NtlmRemoteAuthenticationSettings) settings.proxyAuthentication
            ntlmHost = auth.ntlmHost
            ntlmDomain = auth.ntlmDomain
          }
        }
      }
    }
    return [enabled, hostname, port, username, ntlmHost, ntlmDomain]
  }

  private static toRemoteHttpProxySettings(final Boolean enabled,
                                           final String hostname,
                                           final Integer port,
                                           final Boolean auth,
                                           final String username,
                                           final String password,
                                           final String ntlmHost,
                                           final String ntlmDomain)
  {
    if (enabled) {
      def proxy = new DefaultRemoteHttpProxySettings(
          hostname: hostname,
          port: port ?: 0
      )
      if (auth) {
        if (StringUtils.isNotBlank(ntlmHost)) {
          proxy.proxyAuthentication = new NtlmRemoteAuthenticationSettings(username, password, ntlmDomain, ntlmHost)
        }
        else {
          proxy.proxyAuthentication = new UsernamePasswordRemoteAuthenticationSettings(username, password)
        }
      }
      return proxy
    }
    return null
  }

  def static String getPassword(Password password, RemoteAuthenticationSettings settings) {
    if (password?.valid) {
      return password.value
    }
    if (settings instanceof UsernamePasswordRemoteAuthenticationSettings) {
      return settings.password
    }
    return null
  }

}
