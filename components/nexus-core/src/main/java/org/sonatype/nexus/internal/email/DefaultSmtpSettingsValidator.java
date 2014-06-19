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
package org.sonatype.nexus.internal.email;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.micromailer.imp.DefaultMailType;
import org.sonatype.nexus.email.EmailerException;
import org.sonatype.nexus.email.SmtpConfiguration;
import org.sonatype.nexus.email.SmtpSessionParametersCustomizer;
import org.sonatype.nexus.email.SmtpSettingsValidator;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link SmtpSettingsValidator}.
 */
@Named
@Singleton
public class DefaultSmtpSettingsValidator
    extends ComponentSupport
    implements SmtpSettingsValidator
{
  private static final String NEXUS_MAIL_ID = "Nexus";

  private final EMailer emailer;

  private final List<SmtpSessionParametersCustomizer> customizers;

  @Inject
  public DefaultSmtpSettingsValidator(final EMailer emailer,
                                      final List<SmtpSessionParametersCustomizer> customizers)
  {
    this.emailer = checkNotNull(emailer);
    this.customizers = checkNotNull(customizers);
  }

  public boolean sendSmtpConfigurationTest(final SmtpConfiguration config, final String email) throws EmailerException {
    final EmailerConfiguration emailerConfiguration = new NexusEmailerConfiguration(customizers);
    emailerConfiguration.setDebug(config.isDebugMode());
    emailerConfiguration.setMailHost(config.getHostname());
    emailerConfiguration.setMailPort(config.getPort());
    emailerConfiguration.setUsername(config.getUsername());
    emailerConfiguration.setPassword(config.getPassword());
    emailerConfiguration.setSsl(config.isSslEnabled());
    emailerConfiguration.setTls(config.isTlsEnabled());

    emailer.configure(emailerConfiguration);

    MailRequest request = new MailRequest(NEXUS_MAIL_ID, DefaultMailType.DEFAULT_TYPE_ID);
    request.setFrom(new Address(config.getSystemEmailAddress(), "Nexus Repository Manager"));
    request.getToAddresses().add(new Address(email));
    request.getBodyContext().put(DefaultMailType.SUBJECT_KEY, "Nexus: SMTP Configuration validation.");

    StringBuilder body = new StringBuilder();
    body.append("Your current SMTP configuration is valid!");

    request.getBodyContext().put(DefaultMailType.BODY_KEY, body.toString());

    MailRequestStatus status = emailer.sendSyncedMail(request);

    if (status.getErrorCause() != null) {
      log.error("Unable to send e-mail", status.getErrorCause());
      throw new EmailerException("Unable to send e-mail", status.getErrorCause());
    }

    return status.isSent();
  }
}
