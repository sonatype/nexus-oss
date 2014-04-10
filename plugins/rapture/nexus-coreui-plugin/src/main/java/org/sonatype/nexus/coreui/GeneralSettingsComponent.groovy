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
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * General System Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_GeneralSettings')
class GeneralSettingsComponent
extends DirectComponentSupport
{

  @Inject
  GlobalRestApiSettings globalRestApiSettings

  @Inject
  NexusConfiguration nexusConfiguration

  /**
   * Retrieves general system settings.
   * @return general system settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  GeneralSettingsXO read() {
    return new GeneralSettingsXO(
        baseUrl: globalRestApiSettings.baseUrl,
        forceBaseUrl: globalRestApiSettings.forceBaseUrl
    )
  }

  /**
   * Updates general system settings.
   * @return updated general system settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  GeneralSettingsXO update(final @NotNull(message = '[generalSettings] may not be null') @Valid GeneralSettingsXO generalSettingsXO) {
    validate(generalSettingsXO)
    globalRestApiSettings.baseUrl = generalSettingsXO.baseUrl
    globalRestApiSettings.forceBaseUrl = StringUtils.isBlank(generalSettingsXO.baseUrl) ? false : generalSettingsXO.forceBaseUrl
    nexusConfiguration.saveConfiguration()
    return read()
  }

  private static validate(final GeneralSettingsXO generalSettingsXO) {
    def validations = new ValidationResponse()
    if (!StringUtils.isBlank(generalSettingsXO.baseUrl)) {
      try {
        new URL(generalSettingsXO.baseUrl)
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
