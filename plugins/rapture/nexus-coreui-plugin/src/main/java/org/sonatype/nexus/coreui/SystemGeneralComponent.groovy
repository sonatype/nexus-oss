/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * General System Settings {@link DirectComponent}.
 *
 * @since 2.8
 */
@Named
@Singleton
@DirectAction(action = 'coreui_SystemGeneral')
class SystemGeneralComponent
extends DirectComponentSupport
{
  @Inject
  GlobalRestApiSettings globalRestApiSettings

  @Inject
  NexusConfiguration nexusConfiguration

  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  SystemGeneralXO read() {
    return new SystemGeneralXO(
        baseUrl: globalRestApiSettings.baseUrl,
        forceBaseUrl: globalRestApiSettings.forceBaseUrl
    )
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  SystemGeneralXO update(final SystemGeneralXO xo) {
    validate(xo)
    globalRestApiSettings.baseUrl = xo.baseUrl
    globalRestApiSettings.forceBaseUrl = StringUtils.isBlank(xo.baseUrl) ? null : xo.forceBaseUrl
    nexusConfiguration.saveConfiguration()
    return read()
  }

  private static validate(final SystemGeneralXO xo) {
    def validations = new ValidationResponse()
    if (!StringUtils.isBlank(xo.baseUrl)) {
      try {
        new URL(xo.baseUrl)
      }
      catch (MalformedURLException e) {
        validations.addValidationError(new ValidationMessage('baseUrl', e.message))
      }
    }
    if (!validations.valid) {
      throw new InvalidConfigurationException(validations)
    }
  }
}
