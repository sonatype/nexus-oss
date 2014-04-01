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
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings
import org.sonatype.nexus.configuration.application.GlobalRemoteProxySettings
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.proxy.repository.*

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * HTTP System Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_SystemHttp')
class SystemHttpComponent
extends DirectComponentSupport
{
  @Inject
  GlobalRemoteConnectionSettings connectionSettings

  @Inject
  GlobalRemoteProxySettings proxySettings

  @Inject
  NexusConfiguration nexusConfiguration

  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  SystemHttpXO read() {
    def xo = new SystemHttpXO(
        userAgentCustomisation: connectionSettings.userAgentCustomizationString,
        urlParameters: connectionSettings.queryString,
        timeout: connectionSettings.connectionTimeout,
        retries: connectionSettings.retrievalRetryCount,
        nonProxyHosts: proxySettings.nonProxyHosts
    )

    def (enabled, host, port, username, ntlmHost, ntlmDomain) = fromRemoteHttpProxySettings(proxySettings.httpProxySettings)
    xo.httpEnabled = enabled
    xo.httpHost = host
    xo.httpPort = port
    xo.httpAuthEnabled = StringUtils.isNotBlank(username as String)
    xo.httpAuthUsername = username
    xo.httpAuthPassword = Password.fakePassword()
    xo.httpAuthNtlmHost = ntlmHost
    xo.httpAuthNtlmDomain = ntlmDomain

    (enabled, host, port, username, ntlmHost, ntlmDomain) = fromRemoteHttpProxySettings(proxySettings.httpsProxySettings)
    xo.httpsEnabled = enabled
    xo.httpsHost = host
    xo.httpsPort = port
    xo.httpsAuthEnabled = StringUtils.isNotBlank(username as String)
    xo.httpsAuthUsername = username
    xo.httpAuthPassword = Password.fakePassword()
    xo.httpsAuthNtlmHost = ntlmHost
    xo.httpsAuthNtlmDomain = ntlmDomain

    return xo
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  SystemHttpXO update(final SystemHttpXO xo) {
    validate(xo)

    connectionSettings.userAgentCustomizationString = xo.userAgentCustomisation
    connectionSettings.queryString = xo.urlParameters
    connectionSettings.connectionTimeout = xo.timeout
    connectionSettings.retrievalRetryCount = xo.retries

    proxySettings.nonProxyHosts = xo.httpEnabled ? xo.nonProxyHosts : null
    proxySettings.httpProxySettings = toRemoteHttpProxySettings(
        xo.httpEnabled, xo.httpHost, xo.httpPort,
        xo.httpAuthEnabled,
        xo.httpAuthUsername, getPassword(xo.httpAuthPassword, proxySettings.httpProxySettings?.proxyAuthentication),
        xo.httpAuthNtlmHost, xo.httpAuthNtlmDomain
    )
    proxySettings.httpsProxySettings = toRemoteHttpProxySettings(
        xo.httpsEnabled, xo.httpsHost, xo.httpsPort,
        xo.httpsAuthEnabled,
        xo.httpsAuthUsername, getPassword(xo.httpsAuthPassword, proxySettings.httpsProxySettings?.proxyAuthentication),
        xo.httpsAuthNtlmHost, xo.httpsAuthNtlmDomain
    )

    nexusConfiguration.saveConfiguration()
    return read()
  }

  def static validate(final SystemHttpXO xo) {
    def validations = new ValidationResponse()
    // TODO validate
    if (!validations.valid) {
      throw new InvalidConfigurationException(validations)
    }
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
          port: port
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
