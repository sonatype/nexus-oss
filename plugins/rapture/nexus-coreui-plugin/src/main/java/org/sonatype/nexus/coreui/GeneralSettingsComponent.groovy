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

import org.sonatype.nexus.common.app.BaseUrlManager
import org.sonatype.nexus.common.text.Strings2
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.validation.ValidationMessage
import org.sonatype.nexus.validation.ValidationResponse
import org.sonatype.nexus.validation.ValidationResponseException

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import groovy.transform.PackageScope
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions

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
  BaseUrlManager baseUrlManager

  /**
   * Retrieves general system settings.
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  GeneralSettingsXO read() {
    return new GeneralSettingsXO(
        baseUrl: baseUrlManager.url
    )
  }

  /**
   * Updates general system settings.
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  GeneralSettingsXO update(
      final @NotNull(message = '[generalSettings] may not be null') @Valid GeneralSettingsXO generalSettingsXO)
  {
    validate(generalSettingsXO)
    baseUrlManager.url = generalSettingsXO.baseUrl
    return read()
  }

  // FIXME: use bean-validation constraint

  @PackageScope
  def validate(final GeneralSettingsXO generalSettingsXO) {
    def validations = new ValidationResponse()
    if (!Strings2.isBlank(generalSettingsXO.baseUrl)) {
      try {
        new URL(generalSettingsXO.baseUrl)
      }
      catch (MalformedURLException e) {
        validations.addError(new ValidationMessage('baseUrl', e.message))
      }
    }
    if (!validations.valid) {
      throw new ValidationResponseException(validations)
    }
  }
}
