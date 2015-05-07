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

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.httpclient.HttpClientManager
import org.sonatype.nexus.httpclient.config.ConnectionConfiguration
import org.sonatype.nexus.httpclient.config.HttpClientConfiguration
import org.sonatype.nexus.httpclient.config.ProxyConfiguration
import org.sonatype.nexus.validation.Validate

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import groovy.transform.PackageScope
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions

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
  HttpClientManager httpClientManager

  /**
   * Retrieves HTTP system settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  HttpSettingsXO read() {
    return convert(httpClientManager.configuration)
  }

  @PackageScope
  HttpSettingsXO convert(final HttpClientConfiguration value) {
    HttpSettingsXO result = new HttpSettingsXO()

    value.connection?.with {
      result.timeout = timeout
      result.retries = maximumRetries
    }

    value.proxy?.with {
      http?.with {
        result.httpEnabled = enabled
        result.httpHost = host
        result.httpPort = port
        // TODO: authentication
      }

      https?.with {
        result.httpsEnabled = enabled
        result.httpsHost = host
        result.httpsPort = port
        // TODO: authentication
      }
    }

    // ignore authentication, this is not exposed for global configuration

    return result
  }

  /**
   * Updates HTTP system settings.
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  HttpSettingsXO update(final @NotNull(message = '[httpSettingsXO] may not be null') @Valid HttpSettingsXO settings) {
    httpClientManager.configuration = convert(settings)
    return read()
  }

  //
  // FIXME: overly complex conversion due to lack of nested structure in exchange-object payload
  //

  @PackageScope
  HttpClientConfiguration convert(final HttpSettingsXO value) {
    def result = new HttpClientConfiguration()

    // convert connection configuration
    def connection = {
      if (result.connection == null) {
        result.connection = new ConnectionConfiguration()
      }
      return result.connection
    }

    if (value.timeout != null) {
      connection().timeout = value.timeout
    }
    if (value.retries != null) {
      connection().maximumRetries = value.retries
    }

    // convert proxy configuration
    def proxy = {
      if (result.proxy == null) {
        result.proxy = new ProxyConfiguration()
      }
      return result.proxy
    }

    // http proxy
    if (value.httpEnabled) {
      // TODO: proxy configuration
    }

    // https proxy
    if (value.httpsEnabled) {
      // TODO: proxy configuration
    }

    // ignore authentication, this is not exposed for global configuration

    return result
  }
}
