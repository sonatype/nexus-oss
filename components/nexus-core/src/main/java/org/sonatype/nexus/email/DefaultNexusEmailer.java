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

package org.sonatype.nexus.email;

import java.util.List;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.EMailer;
import org.sonatype.micromailer.EmailerConfiguration;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.micromailer.MailRequestStatus;
import org.sonatype.micromailer.MailType;
import org.sonatype.micromailer.imp.DefaultMailType;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.CSmtpConfigurationCoreConfiguration;
import org.sonatype.sisu.goodies.common.SimpleFormat;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = NexusEmailer.class)
public class DefaultNexusEmailer
    extends AbstractConfigurable
    implements NexusEmailer, Startable
{

  private static final Logger LOG = LoggerFactory.getLogger(DefaultNexusEmailer.class);

  /**
   * The "name" of Nexus instance, as displayed on sent mails.
   */
  private static final String NEXUS_SENDER_NAME = "Nexus Repository Manager";

  /**
   * Custom header to deisgnate Nexus instance as sender
   */
  private static final String X_MESSAGE_SENDER_HEADER = "X-EMailer-Mail-Sender";

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  @Requirement
  private GlobalRestApiSettings globalRestApiSettings;

  @Requirement
  private ApplicationStatusSource applicationStatusSource;

  @Requirement
  private EMailer eMailer;

  @Requirement(role = SmtpSessionParametersCustomizer.class)
  private List<SmtpSessionParametersCustomizer> customizers;

  // ==

  public void start() {
    // nop
  }

  public void stop() {
    getEMailer().shutdown();
  }

  // ==

  public EMailer getEMailer() {
    return eMailer;
  }

  public String getDefaultMailTypeId() {
    return DefaultMailType.DEFAULT_TYPE_ID;
  }

  public MailRequest getDefaultMailRequest(String subject, String body) {
    MailRequest request = new MailRequest(getMailId(), getDefaultMailTypeId());

    request.getCustomHeaders().put(X_MESSAGE_SENDER_HEADER, getSenderId());

    request.setFrom(getSMTPSystemEmailAddress());

    request.getBodyContext().put(DefaultMailType.SUBJECT_KEY, subject);

    request.getBodyContext().put(DefaultMailType.BODY_KEY, body);

    return request;
  }

  public MailRequestStatus sendMail(MailRequest request) {
    if (request.getFrom() == null) {
      request.setFrom(getSMTPSystemEmailAddress());
    }

    prependNexusBaseUrl(request);

    if (emailSettingsConfigured()) {
      return eMailer.sendMail(request);
    }

    final String message = SimpleFormat.format(
        "Mail requestId[%s] not sent, SMTP not configured", request.getRequestId()
    );

    LOG.debug(message);

    final MailRequestStatus status = new MailRequestStatus(request);
    status.setErrorCause(new EmailerException(message));
    return status;
  }

  /**
   * Prepend to message body a link to this Nexus instance (base server URL).
   */
  private void prependNexusBaseUrl(final MailRequest request) {
    final String baseNexusUrl = globalRestApiSettings.getBaseUrl();
    final MailType mailType = eMailer.getMailTypeSource().getMailType(request.getMailTypeId());

    final StringBuilder messageBody = new StringBuilder().append("Message from: ");

    if (mailType != null && mailType.isBodyIsHtml()) {
      messageBody
          .append(
              StringUtils.isNotBlank(baseNexusUrl)
                  ? String.format("<a href=\"%s\">%s</a>", baseNexusUrl, baseNexusUrl)
                  : "<i>(Set the Base URL parameter in Nexus Server Administration to include in future emails)</i>"
          )
          .append("<br><br>");
    }
    else {
      messageBody
          .append(
              StringUtils.isNotBlank(baseNexusUrl)
                  ? baseNexusUrl
                  : "(Set the Base URL parameter in Nexus Server Administration to include in future emails)"
          )
          .append("\n\n");
    }

    messageBody.append(request.getBodyContext().get(DefaultMailType.BODY_KEY));

    request.getBodyContext().put(DefaultMailType.BODY_KEY, messageBody.toString());
  }

  private boolean emailSettingsConfigured() {
    return !("smtp-host".equals(getSMTPHostname())
        && 25 == getSMTPPort()
        && "smtp-username".equals(getSMTPUsername())
    );
  }

  // ==

  @Override
  protected void initializeConfiguration()
      throws ConfigurationException
  {
    if (getApplicationConfiguration().getConfigurationModel() != null) {
      configure(getApplicationConfiguration());
    }
  }


  @Override
  protected ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  @Override
  protected Configurator getConfigurator() {
    return null;
  }

  @Override
  protected CSmtpConfiguration getCurrentConfiguration(boolean forWrite) {
    return ((CSmtpConfigurationCoreConfiguration) getCurrentCoreConfiguration()).getConfiguration(forWrite);
  }

  @Override
  protected CoreConfiguration wrapConfiguration(Object configuration)
      throws ConfigurationException
  {
    if (configuration instanceof ApplicationConfiguration) {
      return new CSmtpConfigurationCoreConfiguration((ApplicationConfiguration) configuration);
    }
    else {
      throw new ConfigurationException("The passed configuration object is of class \""
          + configuration.getClass().getName() + "\" and not the required \""
          + ApplicationConfiguration.class.getName() + "\"!");
    }
  }

  @Override
  public void doConfigure()
      throws ConfigurationException
  {
    super.doConfigure();

    configureEmailer();
  }

  private synchronized void configureEmailer() {
    final EmailerConfiguration config = new NexusEmailerConfiguration(customizers);
    config.setDebug(isSMTPDebug());
    config.setMailHost(getSMTPHostname());
    config.setMailPort(getSMTPPort());
    config.setSsl(isSMTPSslEnabled());
    config.setTls(isSMTPTlsEnabled());
    config.setUsername(getSMTPUsername());
    config.setPassword(getSMTPPassword());

    eMailer.configure(config);
  }

  // ==

  public String getSMTPHostname() {
    return getCurrentConfiguration(false).getHostname();
  }

  public void setSMTPHostname(String host) {
    getCurrentConfiguration(true).setHostname(host);
  }

  public int getSMTPPort() {
    return getCurrentConfiguration(false).getPort();
  }

  public void setSMTPPort(int port) {
    getCurrentConfiguration(true).setPort(port);
  }

  public String getSMTPUsername() {
    return getCurrentConfiguration(false).getUsername();
  }

  public void setSMTPUsername(String username) {
    getCurrentConfiguration(true).setUsername(username);
  }

  public String getSMTPPassword() {
    return getCurrentConfiguration(false).getPassword();
  }

  public void setSMTPPassword(String password) {
    getCurrentConfiguration(true).setPassword(password);
  }

  public Address getSMTPSystemEmailAddress() {
    return new Address(getCurrentConfiguration(false).getSystemEmailAddress(), NEXUS_SENDER_NAME);
  }

  public void setSMTPSystemEmailAddress(Address adr) {
    getCurrentConfiguration(true).setSystemEmailAddress(adr.getMailAddress());
  }

  public boolean isSMTPDebug() {
    return getCurrentConfiguration(false).isDebugMode();
  }

  public void setSMTPDebug(boolean val) {
    getCurrentConfiguration(true).setDebugMode(val);
  }

  public boolean isSMTPSslEnabled() {
    return getCurrentConfiguration(false).isSslEnabled();
  }

  public void setSMTPSslEnabled(boolean val) {
    getCurrentConfiguration(true).setSslEnabled(val);
  }

  public boolean isSMTPTlsEnabled() {
    return getCurrentConfiguration(false).isTlsEnabled();
  }

  public void setSMTPTlsEnabled(boolean val) {
    getCurrentConfiguration(true).setTlsEnabled(val);
  }

  // ==

  protected String getMailId() {
    StringBuilder sb = new StringBuilder("NX");

    sb.append(String.valueOf(System.currentTimeMillis()));

    return sb.toString();

  }

  // ==
  // TODO: this is a workaround, see NXCM-363

  /**
   * The edtion, that will tell us is there some change happened with installation.
   */
  private String platformEditionShort;

  /**
   * The lazily calculated invariant part of the UserAgentString.
   */
  private String userAgentPlatformInfo;

  protected String getSenderId() {
    SystemStatus status = applicationStatusSource.getSystemStatus();

    if (platformEditionShort == null || !platformEditionShort.equals(status.getEditionShort())
        || userAgentPlatformInfo == null) {
      // make it "remember" to be able to detect license changes later
      platformEditionShort = status.getEditionShort();

      userAgentPlatformInfo =
          new StringBuilder("Nexus/").append(status.getVersion()).append(" (")
              .append(status.getEditionShort()).append("; ").append(System.getProperty("os.name"))
              .append("; ").append(System.getProperty("os.version")).append("; ")
              .append(System.getProperty("os.arch")).append("; ")
              .append(System.getProperty("java.version")).append(") ").toString();
    }

    return userAgentPlatformInfo;
  }

  public String getName() {
    return "SMTP Settings";
  }
}
