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
import org.apache.bval.constraints.Email
import org.apache.bval.guice.Validate
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.micromailer.Address
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.configuration.model.CSmtpConfiguration
import org.sonatype.nexus.email.EmailerException
import org.sonatype.nexus.email.NexusEmailer
import org.sonatype.nexus.email.SmtpSettingsValidator
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.notification.NotificationManager
import org.sonatype.nexus.notification.NotificationTarget
import org.sonatype.nexus.rapture.TrustStoreKeys

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLPeerUnverifiedException
import javax.validation.Valid
import javax.validation.constraints.NotNull
import java.security.cert.CertPathBuilderException

import static org.sonatype.nexus.notification.NotificationCheat.AUTO_BLOCK_NOTIFICATION_GROUP_ID

/**
 * Notifications System Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_NotificationSettings')
class NotificationSettingsComponent
extends DirectComponentSupport
{

  private static final TRUST_STORE_TYPE = 'smtp'

  private static final TRUST_STORE_ID = 'global'

  @Inject
  NotificationManager notificationManager

  @Inject
  NexusEmailer emailer

  @Inject
  SmtpSettingsValidator smtpSettingsValidator

  @Inject
  NexusConfiguration nexusConfiguration

  @Inject
  @Nullable
  TrustStoreKeys trustStoreKeys

  /**
   * Retrieves notification system settings
   * @return notification system settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  NotificationSettingsXO read() {
    NotificationTarget notificationTarget = notificationManager.readNotificationTarget(AUTO_BLOCK_NOTIFICATION_GROUP_ID)
    return new NotificationSettingsXO(
        enabled: notificationManager.enabled,
        notifyEmails: notificationTarget?.externalTargets,
        notifyRoles: notificationTarget?.targetRoles,
        smtpHost: emailer.SMTPHostname,
        smtpPort: emailer.SMTPPort,
        smtpUsername: emailer.SMTPUsername,
        smtpPassword: Password.fakePassword(),
        smtpConnectionType: getConnectionType(emailer),
        useTrustStoreForSmtp: trustStoreKeys?.isEnabled(TRUST_STORE_TYPE, TRUST_STORE_ID),
        systemEmail: emailer.SMTPSystemEmailAddress.mailAddress
    )
  }

  /**
   * Updates notification system settings
   * @return updated notification system settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  NotificationSettingsXO update(final @NotNull(message = '[notificationSettings] may not be null') @Valid NotificationSettingsXO notificationSettingsXO) {
    notificationManager.enabled = notificationSettingsXO.enabled

    NotificationTarget notificationTarget = new NotificationTarget()
    if (notificationSettingsXO.notifyEmails) notificationTarget.externalTargets.addAll(notificationSettingsXO.notifyEmails)
    if (notificationSettingsXO.notifyRoles) notificationTarget.targetRoles.addAll(notificationSettingsXO.notifyRoles)
    notificationManager.updateNotificationTarget(notificationTarget)

    emailer.with {
      SMTPHostname = notificationSettingsXO.smtpHost ?: ''
      if (notificationSettingsXO.smtpPort) SMTPPort = notificationSettingsXO.smtpPort
      SMTPUsername = notificationSettingsXO.smtpUsername
      if (notificationSettingsXO.smtpPassword?.valid) SMTPPassword = notificationSettingsXO.smtpPassword.value
      SMTPSystemEmailAddress = new Address(notificationSettingsXO.systemEmail?.trim())
      SMTPSslEnabled = notificationSettingsXO.smtpConnectionType == NotificationSettingsXO.ConnectionType.SSL
      SMTPTlsEnabled = notificationSettingsXO.smtpConnectionType == NotificationSettingsXO.ConnectionType.TLS
    }
    nexusConfiguration.saveConfiguration()
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, TRUST_STORE_ID, notificationSettingsXO.useTrustStoreForSmtp)
    return read()
  }

  /**
   * Verifies SMTP connection.
   * @param notificationSettingsXO valid SMTP settings
   * @param email to send verification email to
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  void verifyConnection(final @NotNull(message = '[notificationSettings] may not be null') @Valid NotificationSettingsXO notificationSettingsXO,
                        final @NotNull(message = '[email] may not be null') @Email String email)
  {
    try {
      smtpSettingsValidator.sendSmtpConfigurationTest(
          new CSmtpConfiguration(
              hostname: notificationSettingsXO.smtpHost,
              port: notificationSettingsXO.smtpPort,
              username: notificationSettingsXO.smtpUsername,
              password: notificationSettingsXO.smtpPassword?.valid ? notificationSettingsXO.smtpPassword : emailer.SMTPPassword,
              sslEnabled: notificationSettingsXO.smtpConnectionType == NotificationSettingsXO.ConnectionType.SSL,
              tlsEnabled: notificationSettingsXO.smtpConnectionType == NotificationSettingsXO.ConnectionType.TLS,
              systemEmailAddress: notificationSettingsXO.systemEmail
          ),
          email
      )
    }
    catch (EmailerException e) {
      throw new Exception(
          'Failed to send validation e-mail: ' + parseReason(e)
      )
    }
  }

  private static NotificationSettingsXO.ConnectionType getConnectionType(final NexusEmailer emailer) {
    if (emailer.SMTPSslEnabled) {
      return NotificationSettingsXO.ConnectionType.SSL
    }
    if (emailer.SMTPTlsEnabled) {
      return NotificationSettingsXO.ConnectionType.TLS
    }
    return NotificationSettingsXO.ConnectionType.PLAIN
  }

  private static String parseReason(final EmailerException e) {
    // first let's go to the top in exception chain
    Throwable top = e
    while (top.getCause() != null) {
      top = top.getCause()
    }
    if (top instanceof SSLPeerUnverifiedException) {
      return "Untrusted Remote"
    }
    if (top instanceof CertPathBuilderException) {
      return "Untrusted Remote (" + top.getMessage() + ")"
    }
    if (top instanceof UnknownHostException) {
      return "Unknown host '" + top.getMessage() + "'"
    }
    return top.getMessage()
  }

}
