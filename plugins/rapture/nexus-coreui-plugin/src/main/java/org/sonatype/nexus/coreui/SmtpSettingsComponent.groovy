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
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.Email
import org.sonatype.micromailer.Address
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.email.EmailerException
import org.sonatype.nexus.email.NexusEmailer
import org.sonatype.nexus.email.SmtpConfiguration
import org.sonatype.nexus.email.SmtpSettingsValidator
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.guice.Validate
import org.sonatype.nexus.rapture.TrustStoreKeys

import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLPeerUnverifiedException
import javax.validation.Valid
import javax.validation.constraints.NotNull
import java.security.cert.CertPathBuilderException

/**
 * SMTP System Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_SmtpSettings')
class SmtpSettingsComponent
extends DirectComponentSupport
{

  private static final TRUST_STORE_TYPE = 'smtp'

  private static final TRUST_STORE_ID = 'global'

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
  SmtpSettingsXO read() {
    return new SmtpSettingsXO(
        systemEmail: emailer.SMTPSystemEmailAddress.mailAddress,
        host: emailer.SMTPHostname,
        port: emailer.SMTPPort,
        username: emailer.SMTPUsername,
        password: Password.fakePassword(),
        connectionType: getConnectionType(emailer),
        useTrustStore: trustStoreKeys?.isEnabled(TRUST_STORE_TYPE, TRUST_STORE_ID)
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
  SmtpSettingsXO update(final @NotNull(message = '[smtpSettingsXO] may not be null') @Valid SmtpSettingsXO smtpSettingsXO) {
    emailer.with {
      SMTPHostname = smtpSettingsXO.host ?: ''
      if (smtpSettingsXO.port) SMTPPort = smtpSettingsXO.port
      SMTPUsername = smtpSettingsXO.username
      if (smtpSettingsXO.password?.valid) SMTPPassword = smtpSettingsXO.password.value
      SMTPSystemEmailAddress = new Address(smtpSettingsXO.systemEmail?.trim())
      SMTPSslEnabled = smtpSettingsXO.connectionType == SmtpSettingsXO.ConnectionType.SSL
      SMTPTlsEnabled = smtpSettingsXO.connectionType == SmtpSettingsXO.ConnectionType.TLS
    }
    nexusConfiguration.saveConfiguration()
    trustStoreKeys?.setEnabled(TRUST_STORE_TYPE, TRUST_STORE_ID, smtpSettingsXO.useTrustStore)
    return read()
  }

  /**
   * Verifies SMTP connection.
   * @param smtpSettingsXO valid SMTP settings
   * @param email to send verification email to
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  void verifyConnection(final @NotNull(message = '[smtpSettingsXO] may not be null') @Valid SmtpSettingsXO smtpSettingsXO,
                        final @NotNull(message = '[email] may not be null') @Email String email)
  {
    try {
      smtpSettingsValidator.sendSmtpConfigurationTest(
          new SmtpConfiguration(
              hostname: smtpSettingsXO.host,
              port: smtpSettingsXO.port,
              username: smtpSettingsXO.username,
              password: smtpSettingsXO.password?.valid ? smtpSettingsXO.password : emailer.SMTPPassword,
              sslEnabled: smtpSettingsXO.connectionType == SmtpSettingsXO.ConnectionType.SSL,
              tlsEnabled: smtpSettingsXO.connectionType == SmtpSettingsXO.ConnectionType.TLS,
              systemEmailAddress: smtpSettingsXO.systemEmail
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

  private static SmtpSettingsXO.ConnectionType getConnectionType(final NexusEmailer emailer) {
    if (emailer.SMTPSslEnabled) {
      return SmtpSettingsXO.ConnectionType.SSL
    }
    if (emailer.SMTPTlsEnabled) {
      return SmtpSettingsXO.ConnectionType.TLS
    }
    return SmtpSettingsXO.ConnectionType.PLAIN
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
      return "Untrusted Remote (${top.message})"
    }
    if (top instanceof UnknownHostException) {
      return "Unknown Host (${top.message})"
    }
    return top.getMessage()
  }

}
