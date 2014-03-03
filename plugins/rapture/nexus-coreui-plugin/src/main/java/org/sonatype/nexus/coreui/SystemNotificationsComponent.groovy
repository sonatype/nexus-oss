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
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.micromailer.Address
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.email.NexusEmailer
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.rapture.TrustStore

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Notifications System Settings {@link DirectComponent}.
 *
 * @since 2.8
 */
@Named
@Singleton
@DirectAction(action = 'coreui_SystemNotifications')
class SystemNotificationsComponent
extends DirectComponentSupport
{

  private static final TRUST_STORE_TYPE = 'smtp'

  private static final TRUST_STORE_ID = 'global'

  @Inject
  NexusEmailer emailer

  @Inject
  NexusConfiguration nexusConfiguration

  @Inject
  @Nullable
  TrustStore trustStore

  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  SystemNotificationsXO read() {
    return new SystemNotificationsXO(
        host: emailer.SMTPHostname,
        port: emailer.SMTPPort,
        username: emailer.SMTPUsername,
        password: Password.fakePassword(),
        connectionType: getConnectionType(emailer),
        useTrustStoreForSmtp: trustStore?.isEnabled(TRUST_STORE_TYPE, TRUST_STORE_ID),
        systemEmail: emailer.SMTPSystemEmailAddress.mailAddress
    )
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  SystemNotificationsXO update(final SystemNotificationsXO notificationsXO) {
    assert notificationsXO
    emailer.with {
      SMTPHostname = notificationsXO.host ?: ''
      if (notificationsXO.port) SMTPPort = notificationsXO.port
      SMTPUsername = notificationsXO.username
      if (notificationsXO.password?.valid) SMTPPassword = notificationsXO.password.value
      SMTPSystemEmailAddress = new Address(notificationsXO.systemEmail?.trim())
      SMTPSslEnabled = notificationsXO.connectionType == SystemNotificationsXO.ConnectionType.SSL
      SMTPTlsEnabled = notificationsXO.connectionType == SystemNotificationsXO.ConnectionType.TLS
    }
    nexusConfiguration.saveConfiguration()
    trustStore?.setEnabled(TRUST_STORE_TYPE, TRUST_STORE_ID, notificationsXO.useTrustStoreForSmtp)
    return read()
  }

  private static SystemNotificationsXO.ConnectionType getConnectionType(final NexusEmailer emailer) {
    if (emailer.SMTPSslEnabled) {
      return SystemNotificationsXO.ConnectionType.SSL
    }
    if (emailer.SMTPTlsEnabled) {
      return SystemNotificationsXO.ConnectionType.TLS
    }
    return SystemNotificationsXO.ConnectionType.PLAIN
  }

}
