/*
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
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.security.SecuritySystem

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Anonymous Security Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_SecurityAnonymous')
class SecurityAnonymousComponent
extends DirectComponentSupport
{

  @Inject
  NexusConfiguration nexusConfiguration

  @Inject
  SecuritySystem securitySystem

  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  SecurityAnonymousXO read() {
    boolean customUser = nexusConfiguration.anonymousUsername != 'anonymous'
    return new SecurityAnonymousXO(
        enabled: nexusConfiguration.anonymousAccessEnabled,
        useCustomUser: customUser,
        username: customUser ? nexusConfiguration.anonymousUsername : null,
        password: customUser ? Password.fakePassword() : null
    )
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  SecurityAnonymousXO update(final SecurityAnonymousXO anonymousXO) {
    def username, password
    if (anonymousXO.enabled) {
      if (anonymousXO.useCustomUser) {
        username = anonymousXO.username
        password = anonymousXO.password?.valueIfValid ?: nexusConfiguration.anonymousPassword
      }
      else {
        username = 'anonymous'
        password = 'anonymous' // FIXME is there another way to get default anonymous user/password
      }
    }
    else {
      username = null
      password = null
    }
    nexusConfiguration.setAnonymousAccess(anonymousXO.enabled, username, password)
    nexusConfiguration.saveConfiguration()
    return read()
  }

}
